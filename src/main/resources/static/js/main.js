document.addEventListener('DOMContentLoaded', () => {

    /* ============================================================
       [핵심] 로그인/소셜 로그인 성공 후 메인 진입 시 토스트 처리
    ============================================================ */
    const params = new URLSearchParams(window.location.search);

    // 1. 일반 로그인 or 소셜 로그인 성공 파라미터 확인
    const isLoginSuccess = params.get('loginSuccess') === 'true';
    const isOauthSuccess = params.get('oauthSuccess') === 'true';

    if (isLoginSuccess || isOauthSuccess) {
        const username = params.get('username') || '회원';

        // 메인 페이지에서 토스트 띄우기
        showToast(`로그인 성공!\n${username}님 환영합니다!`);

        // URL에서 지저분한 파라미터 제거 (새로고침 시 토스트 반복 방지)
        const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
        window.history.replaceState({ path: cleanUrl }, '', cleanUrl);
    }

    /* ============================================================
       1-2. 소셜 로그인 에러 처리
    ============================================================ */
    const oauthError = params.get('oauthError');
    if (oauthError && !window.oauthErrorShown) {
        window.oauthErrorShown = true;
        let message = decodeURIComponent(oauthError).replace(/\+/g, ' ');
        const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
        window.history.replaceState({ path: cleanUrl }, '', cleanUrl);
        showToast(message, 'error'); // 에러 타입으로 토스트 표시
    }

    /* ============================================================
       2. DOM 요소 선택 (기존 코드 유지)
    ============================================================ */
    const loginModalOverlay   = document.getElementById('login-modal-overlay');
    const signupModalOverlay  = document.getElementById('signup-modal-overlay');
    const loginModalTrigger   = document.getElementById('login-modal-trigger');
    const switchToSignupBtn   = document.getElementById('switch-to-signup-trigger');
    const loginModalClose     = document.getElementById('login-modal-close');
    const signupModalClose    = document.getElementById('signup-modal-close');
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
        es.onerror = () => { es.close(); };

        es.addEventListener("order-complete", async (event) => {
            const order = JSON.parse(event.data);
            const menuName = order.orderItemList?.[0]?.menuItemName || "";
            const dailyNum = String(order.dailyOrderNum).padStart(4, "0");

            showToast(`'${menuName}' 주문이 완료되었습니다.`);
            showAlarmDot();
            addNotificationCard(dailyNum, menuName);
            await loadUserOrders();
        });
        return es;
    }

    async function initUserSSE() {
        if (typeof IS_LOGGED_IN === 'undefined' || !IS_LOGGED_IN) return;
        if (typeof USER_ID === 'undefined' || !USER_ID) return;

        const regionResp = await fetch("/home/getRegion");
        const storeName = await regionResp.text();
        if (!storeName || storeName === "null" || storeName.trim() === "") return;

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
            }).then(() => {
                window.location.reload();
            }).catch(err => console.error(err));
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
                if(userRegion) userRegion.focus();
                return false;
            }
            window.location.href = '/menu/coffee';
            return true;
        } catch (error) {
            console.error("매장 확인 오류:", error);
            alert("매장 정보를 확인할 수 없습니다.");
            return false;
        }
    }

    if (orderBtn) {
        orderBtn.addEventListener("click", async (e) => {
            e.preventDefault();
            if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
                window.location.href = '/home/login';
                return;
            }
            await checkAndGoToMenu();
        });
    }

    const loginRequiredLinks = document.querySelectorAll('.login-required');
    loginRequiredLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
                e.preventDefault();
                window.location.href = '/home/login';
            }
        });
    });

    /* ============================================================
       11. 초기 알림 상태 점검
    ============================================================ */
    checkEmptyNotifications();
    document.querySelectorAll(".notification-card").forEach(card => {
        initSwipeToDelete(card);
    });
}); // DOMContentLoaded End


/* ============================================================
   [전역 함수] 메시지 & 토스트 유틸
============================================================ */

function clearErrorMessages(formElement, field = null) {
    if (!formElement) return;
    if (field) {
        const target = formElement.querySelector(`.error-message[data-field="${field}"]`);
        if (target) target.textContent = '';
    } else {
        formElement.querySelectorAll('.error-message').forEach(el => (el.textContent = ''));
    }
}

function displayErrorMessage(formElement, field, message) {
    if (!formElement) return;
    const target = formElement.querySelector(`.error-message[data-field="${field}"]`);
    if (target) target.textContent = message;
}

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
        if (movedX < -40) {
            item.classList.add("swiped");
            isSwiped = true;
        }
        if (movedX > 10 && !isSwiped) {
            item.classList.remove("swiped");
        }
    });
    item.addEventListener("touchend", () => {
        if (!isSwiped) item.classList.remove("swiped");
    });

    const deleteBtn = item.querySelector(".delete-btn");
    if (deleteBtn) {
        deleteBtn.addEventListener("click", () => {
            item.style.opacity = "0";
            setTimeout(() => {
                item.remove();
                checkEmptyNotifications();
            }, 250);
        });
    }
}

/* ============================================================
   [필수] showToast 함수 (전역 함수)
============================================================ */
function showToast(message, type = 'success') {
    const toast = document.getElementById("custom-toast");
    const toastText = document.getElementById("toast-text");

    let toastIcon = document.querySelector("#custom-toast .toast-icon");

    if (!toast || !toastText) return;

    toastText.textContent = message;

    if (toastIcon) {
        if(type === 'error') {
            toastIcon.className = "fa-solid fa-circle-xmark toast-icon";
            toastIcon.style.color = "#ff6b6b";
        } else {
            toastIcon.className = "fa-solid fa-check-circle toast-icon";
            toastIcon.style.color = "#51cf66";
        }
    }

    toast.classList.remove("toast-hidden");
    toast.classList.add("toast-visible");

    setTimeout(() => {
        toast.classList.remove("toast-visible");
        toast.classList.add("toast-hidden");
    }, 3000);
}

function showAlarmDot() {
    const dot = document.getElementById("alarm-dot");
    if (dot) dot.style.display = "block";
}

function hideAlarmDot() {
    const dot = document.getElementById("alarm-dot");
    if (dot) dot.style.display = "none";
}

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