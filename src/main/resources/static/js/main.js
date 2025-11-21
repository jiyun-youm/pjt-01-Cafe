document.addEventListener('DOMContentLoaded', () => {

    /* ============================================================
       1. 소셜 로그인 에러 처리 (한 번만 알림)
    ============================================================ */
    const params = new URLSearchParams(window.location.search);
    const oauthError = params.get('oauthError');

    if (oauthError && !window.oauthErrorShown) {
        window.oauthErrorShown = true;

        let message = decodeURIComponent(oauthError).replace(/\+/g, ' ');

        const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
        window.history.replaceState({ path: cleanUrl }, '', cleanUrl);

        alert(message);
    }

    /* ============================================================
       2. DOM 요소 선택
    ============================================================ */
    const loginModalOverlay   = document.getElementById('login-modal-overlay');
    const signupModalOverlay  = document.getElementById('signup-modal-overlay');

    const loginModalTrigger   = document.getElementById('login-modal-trigger');
    const switchToSignupBtn   = document.getElementById('switch-to-signup-trigger');
    const loginModalClose     = document.getElementById('login-modal-close');
    const signupModalClose    = document.getElementById('signup-modal-close');

    const loginForm           = document.getElementById('modalLoginForm');
    const signupForm          = document.getElementById('modalSignupForm');

    const notificationTrigger = document.getElementById('notification-trigger');
    const notificationPopup   = document.getElementById('notification-popup');

    const userRegion          = document.getElementById('userRegion');
    const orderBtn            = document.getElementById('orderBtn');

    /* ============================================================
       3. 로그인 / 회원가입 모달 열기/닫기
    ============================================================ */

    if (loginModalTrigger && loginModalOverlay) {
        loginModalTrigger.addEventListener('click', (e) => {
            e.preventDefault();
            loginModalOverlay.classList.add('show');
        });
    }

    if (loginModalClose && loginModalOverlay) {
        loginModalClose.addEventListener('click', () => {
            loginModalOverlay.classList.remove('show');
        });
    }

    if (signupModalClose && signupModalOverlay) {
        signupModalClose.addEventListener('click', () => {
            signupModalOverlay.classList.remove('show');
        });
    }

    if (switchToSignupBtn && loginModalOverlay && signupModalOverlay) {
        switchToSignupBtn.addEventListener('click', () => {
            loginModalOverlay.classList.remove('show');
            signupModalOverlay.classList.add('show');
        });
    }

    /* ============================================================
       4. 헤더 종모양 알림 팝업
    ============================================================ */

    if (notificationTrigger && notificationPopup) {
        notificationTrigger.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            notificationPopup.classList.toggle('show');
            hideAlarmDot();
            checkEmptyNotifications();
        });

        document.addEventListener('click', (e) => {
            if (!notificationPopup.contains(e.target) &&
                !notificationTrigger.contains(e.target)) {
                notificationPopup.classList.remove('show');
            }
        });
    }

    /* ============================================================
       5. 매장 선택 초기화 (세션값 → selectbox)
    ============================================================ */

    async function initRegionSelect() {
        if (!userRegion) return;

        try {
            const resp = await fetch("/home/getRegion");
            const storeName = await resp.text();

            if (storeName && storeName !== "null" && storeName.trim() !== "") {
                userRegion.value = storeName;
            } else {
                userRegion.value = "none";
            }
        } catch (e) {
            console.error("getRegion error:", e);
        }
    }
    initRegionSelect();

    /* ============================================================
       6. SSE 연결 (사용자용)
    ============================================================ */

    function connectSSE(url) {
        let es = new EventSource(url);

        es.onopen = () => console.log("[USER SSE] Connected");

        es.onerror = () => {
            console.warn("[USER SSE] Disconnected → Reconnecting in 3s...");
            es.close();
            setTimeout(() => connectSSE(url), 3000);
        };

        // 주문 완료 이벤트
        es.addEventListener("order-complete", async (event) => {
            const order = JSON.parse(event.data);

            const menuName = order.orderItemList?.[0]?.menuItemName || "";
            const dailyNum = String(order.dailyOrderNum).padStart(4, "0");

            // 상단 토스트
            showToast(`'${menuName}' 주문이 완료되었습니다.`);

            // 종모양 알림 점
            showAlarmDot();

            // 알림 카드 추가
            addNotificationCard(dailyNum, menuName);

            // 이전 주문내역 갱신
            await loadUserOrders();
        });

        return es;
    }

    async function initUserSSE() {
        // 서버에서 내려주는 전역 상수 사용 (userBaseLayout.html 에서 세팅)
        if (typeof IS_LOGGED_IN === 'undefined' || !IS_LOGGED_IN) return;

        const regionResp = await fetch("/home/getRegion");
        const storeName = await regionResp.text();

        if (!storeName || storeName === "null" || storeName.trim() === "") {
            console.log("[USER SSE] 매장 미선택 → SSE 중지");
            return;
        }

        connectSSE(`/sse/user/${USER_ID}`);
    }

    initUserSSE();

    /* ============================================================
       7. 이전 주문 내역 로딩
    ============================================================ */

    async function loadUserOrders() {

        if (typeof USER_ID === 'undefined' || !USER_ID) return;

        try {
            const resp = await fetch(`/api/orders/user-list?memberId=${USER_ID}`);
            const list = await resp.json();

            const container = document.getElementById("user-order-list");
            if (!container) return;

            container.innerHTML = "";

            list.forEach(order => {
                const div = document.createElement("div");
                div.classList.add("order-item");
                div.innerHTML = `
                    <div class='order-title'>주문번호 #${order.orderId}</div>
                    <div class='order-date'>${order.orderTime}</div>
                    <div class='order-status'>${order.orderStatus}</div>
                `;
                container.appendChild(div);
            });

        } catch (e) {
            console.error("[주문내역 로드 실패]", e);
        }
    }
    loadUserOrders();

    /* ============================================================
       8. 지역 선택 변경 시 세션에 저장
    ============================================================ */

    if (userRegion) {
        userRegion.addEventListener("change", () => {
            const region = userRegion.value;

            fetch("/home/saveRegion", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ region })
            })
                .then(() => console.log("매장 정보 저장:", region))
                .catch(err => console.error(err));
        });
    }

    /* ============================================================
       9. 주문하기 버튼 (로그인 + 매장 선택 체크)
    ============================================================ */

    async function checkAndGoToMenu() {
        try {
            const resp = await fetch("/home/getRegion");
            const storeName = await resp.text();

            if (!storeName || storeName === "null" || storeName.trim() === "") {
                alert("주문할 매장을 먼저 선택해주세요.");
                window.location.href = '/home/';
                return false;
            }

            window.location.href = '/menu/coffee';
            return true;

        } catch (error) {
            console.error("매장 확인 오류:", error);
            alert("매장 정보를 확인할 수 없습니다.");
            window.location.href = '/home/';
            return false;
        }
    }

    if (orderBtn) {
        orderBtn.addEventListener("click", async (e) => {
            e.preventDefault();

            if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
                if (loginModalOverlay) loginModalOverlay.classList.add('show');
                return;
            }

            await checkAndGoToMenu();
        });
    }

    /* ============================================================
       10. 로그인 보호 링크 (찜, 주문내역, 장바구니 등)
    ============================================================ */

    const loginRequiredLinks = document.querySelectorAll('.login-required');
    loginRequiredLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
                e.preventDefault();
                if (loginModalOverlay) loginModalOverlay.classList.add('show');
            }
        });
    });

    /* ============================================================
       11. 초기 알림 상태 점검 (주문 알림 없음 텍스트)
    ============================================================ */

    checkEmptyNotifications();

    // 혹시 서버 사이드에서 기존 notification-card 를 그려줄 경우를 대비
    document.querySelectorAll(".notification-card").forEach(card => {
        initSwipeToDelete(card);
    });
});


/* ============================================================
   공용 메시지 헬퍼 함수들 (다른 JS에서도 사용 가능)
============================================================ */

function clearErrorMessages(formElement, field = null) {
    if (!formElement) return;

    if (field) {
        const target = formElement.querySelector(`.error-message[data-field="${field}"]`);
        if (target) target.textContent = '';
    } else {
        formElement.querySelectorAll('.error-message')
            .forEach(el => (el.textContent = ''));
    }
}

function clearSuccessMessages(formElement, field = null) {
    if (!formElement) return;

    if (field) {
        const target = formElement.querySelector(`.success-message[data-field="${field}"]`);
        if (target) target.textContent = '';
    } else {
        formElement.querySelectorAll('.success-message')
            .forEach(el => (el.textContent = ''));
    }
}

function displayErrorMessage(formElement, field, message) {
    if (!formElement) return;
    const target = formElement.querySelector(`.error-message[data-field="${field}"]`);
    if (target) target.textContent = message;
}

function displaySuccessMessage(formElement, field, message) {
    if (!formElement) return;
    const target = formElement.querySelector(`.success-message[data-field="${field}"]`);
    if (target) target.textContent = message;
}

/* ============================================================
   알림(종모양) 관련 유틸
============================================================ */

function checkEmptyNotifications() {
    const list = document.getElementById("notification-list");
    const emptyMsg = document.getElementById("no-notification");

    if (!list || !emptyMsg) return;

    if (list.children.length === 0) {
        emptyMsg.style.display = "block";
    } else {
        emptyMsg.style.display = "none";
    }
}

/**
 * 알림 카드 1개 추가 (예: “아메리카노가 준비되었습니다.”)
 */
function addNotificationCard(dailyNum, menuName) {
    const list = document.getElementById("notification-list");
    if (!list) return;

    const card = document.createElement("div");
    card.className = "notification-card";

    card.innerHTML = `
        <span>주문번호 ${dailyNum}번 '${menuName}' 주문이 완료되었습니다.</span>
        <button class="delete-btn">삭제</button>
    `;

    list.prepend(card);
    initSwipeToDelete(card);
    checkEmptyNotifications();
}

/**
 * 알림 카드 스와이프 → 삭제 버튼 노출
 */
function initSwipeToDelete(item) {
    let startX = 0;
    let movedX = 0;
    let isSwiped = false;

    item.addEventListener("touchstart", (e) => {
        startX = e.touches[0].clientX;
        isSwiped = false;
    });

    item.addEventListener("touchmove", (e) => {
        movedX = e.touches[0].clientX - startX;

        if (movedX < -40) {               // 왼쪽으로 40px 이상 드래그하면
            item.classList.add("swiped"); // 🔥 버튼 보이기
            isSwiped = true;
        }
        if (movedX > 10 && !isSwiped) {   // 오른쪽으로 다시 밀면 원복
            item.classList.remove("swiped");
        }
    });

    item.addEventListener("touchend", () => {
        if (!isSwiped) {
            item.classList.remove("swiped");
        }
    });

    // 삭제 버튼 클릭 시 페이드아웃 후 DOM 제거
    const deleteBtn = item.querySelector(".delete-btn");
    deleteBtn.addEventListener("click", () => {
        item.style.opacity = "0";
        setTimeout(() => {
            item.remove();
            checkEmptyNotifications();
        }, 250);
    });
}

/* ============================================================
   상단 토스트 (아이폰 알림 스타일)
============================================================ */

function showToast(message) {
    const toast = document.getElementById("toast-notification");
    const toastMsg = document.getElementById("toast-message");

    if (!toast || !toastMsg) return;

    toastMsg.innerText = message;

    toast.classList.remove("hide");
    toast.classList.remove("show");

    setTimeout(() => {
        toast.classList.add("show");
    }, 10);

    setTimeout(() => {
        toast.classList.remove("show");
        toast.classList.add("hide");
    }, 3000);
}

/* 종모양 빨간 점 on/off */
function showAlarmDot() {
    const dot = document.getElementById("alarm-dot");
    if (dot) dot.style.display = "block";
}

function hideAlarmDot() {
    const dot = document.getElementById("alarm-dot");
    if (dot) dot.style.display = "none";
}

/* ============================================================
   모바일 vh 보정 (주소창 영역 제외)
============================================================ */

function setVh() {
    document.documentElement.style.setProperty('--vh', window.innerHeight * 0.01 + 'px');
}
setVh();
window.addEventListener('resize', setVh);

// 쿠폰 페이지로 이동
document.addEventListener("DOMContentLoaded", () => {
    const couponEl = document.getElementById("coupon-count");

    couponEl.addEventListener("click", () => {
        if (!IS_LOGGED_IN) {
            alert("로그인이 필요합니다.");
            return;
        }
        location.href = "/home/coupon";  // 쿠폰 페이지 이동
    });
});