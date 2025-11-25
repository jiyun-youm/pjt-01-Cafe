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
       3. 헤더 종모양 알림 팝업
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
       4. 매장 선택 초기화 (세션값 → selectbox)
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
       5. SSE 연결 (사용자용)
    ============================================================ */
    let userEventSource = null; // 중복 연결 방지용 변수

    function connectSSE(url) {
        if (userEventSource) {
            userEventSource.close();
        }

        console.log(`🔌 [SSE] 사용자 연결 시도: ${url}`);
        let es = new EventSource(url);
        userEventSource = es;

        es.onopen = () => {
            console.log("🟢 [SSE] 사용자 알림 서비스 연결됨");
            checkMissedNotifications();
        };

        es.onerror = () => {
            es.close();
            setTimeout(initUserSSE, 3000);
        };

        // 주문 완료 이벤트 수신
        es.addEventListener("order-complete", async (event) => {
            console.log("🔔 주문 완료 알림 도착:", event.data);
            const order = JSON.parse(event.data);

            const menuName = order.orderItemList?.[0]?.menuItemName || "메뉴";
            const extraCount = (order.orderItemList?.length || 1) - 1;
            const title = extraCount > 0 ? `${menuName} 외 ${extraCount}건` : menuName;
            const dailyNum = String(order.dailyOrderNum).padStart(4, "0");

            localStorage.setItem(`notified_order_${order.orderId}`, "true");

            showToast(`주문번호 ${dailyNum}\n${title} 이(가) 준비되었어요!\n픽업대에서 메뉴를 픽업해주세요!`);
            showAlarmDot();
            addNotificationCard(dailyNum, title);
            await loadUserOrders();
        });

        es.addEventListener("order-cancel", (event) => {
            const order = JSON.parse(event.data);
            const dailyNum = String(order.dailyOrderNum).padStart(4, "0");
            showToast(`주문번호 ${dailyNum}\n고객님의 주문이 취소되었습니다.`,'error');
            showAlarmDot();
        });

        return es;
    }


    function initUserSSE() {
        // 1. 비로그인 상태면 중단
        if (typeof IS_LOGGED_IN === 'undefined' || !IS_LOGGED_IN) {
            return;
        }

        if (typeof USER_ID === 'undefined' || !USER_ID) {
            console.error("❌ [SSE] USER_ID가 없습니다! (세션 로딩 문제 가능성)");
            return;
        }
        const safeUserId = encodeURIComponent(USER_ID);

        console.log(`🔌 [SSE] 초기화 시도. 원본ID: ${USER_ID}, 전송ID: ${safeUserId}`);
        connectSSE(`/sse/user/${safeUserId}`);
    }

    // 페이지 로드 시 즉시 실행
    initUserSSE();

    document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "visible") {
            console.log("👀 화면 활성화됨. SSE 연결 상태 점검...");
            if (!userEventSource || userEventSource.readyState === EventSource.CLOSED) {
                console.log("🔄 SSE 재연결 시도...");
                initUserSSE();
            }
        }
    });

    /* ============================================================
       [추가] 놓친 알림 체크
       - SSE 연결이 끊긴 사이에 완료된 주문이 있는지 확인합니다.
       - 이미 알림을 본 주문은 localStorage에 저장해 중복을 막습니다.
    ============================================================ */
    async function checkMissedNotifications() {
        if (typeof USER_ID === 'undefined' || !USER_ID) return;

        try {
            // 기존에 사용하시던 주문 내역 조회 API 활용
            const resp = await fetch(`/api/orders/user-list?memberId=${USER_ID}`);
            if (!resp.ok) return;

            const list = await resp.json();

            // "제조완료(COMPLETED)" 상태인데, 아직 알림을 안 본 주문 찾기
            list.forEach(order => {
                // 주문 상태가 '제조완료' 인지 확인 (서버의 상태값에 맞춰 수정 필요: COMPLETED, 제조완료 등)
                // 예시: order.orderStatus가 한글 "제조완료" 혹은 영문 "COMPLETED" 라고 가정
                if (order.orderStatus === '제조완료' || order.orderStatus === 'COMPLETED') {

                    const storageKey = `notified_order_${order.orderId}`;

                    // 로컬 스토리지에 기록이 없으면 -> 알림을 못 받은 것임!
                    if (!localStorage.getItem(storageKey)) {
                        console.log(`🔎 놓친 주문 발견! ID: ${order.orderId}`);

                        // 1. 토스트 띄우기
                        const menuName = order.orderItemList?.[0]?.menuItemName || "메뉴";
                        const dailyNum = String(order.dailyOrderNum).padStart(4, "0");
                        showToast(`주문번호 ${dailyNum}\n${menuName} 메뉴가 준비되어 있습니다!\n(미수신 알림)`);

                        // 2. 알림창(종모양) 업데이트
                        showAlarmDot();
                        addNotificationCard(dailyNum, menuName);

                        // 3. "나 이거 봤음" 도장 찍기 (다음에 또 안 뜨게)
                        localStorage.setItem(storageKey, "true");
                    }
                }
            });
        } catch (e) {
            console.error("놓친 알림 체크 중 오류:", e);
        }
    }

    /* ============================================================
       6. 이전 주문 내역 로딩
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
       6-2. 이전 주문 내역 클릭 시 해당 매장의 구매페이지로 이동
    ============================================================ */
    // document.querySelectorAll(".order-item").forEach(item => {
    //     item.addEventListener("click", async () => {
    //
    //         const store = item.dataset.store;  // ex. "강남중앙점"
    //         if (!store) return;
    //
    //         // 1) 지점을 세션에 저장
    //         await fetch("/home/saveRegion", {
    //             method: "POST",
    //             headers: { "Content-Type": "application/json" },
    //             body: JSON.stringify({ region: store })
    //         });
    //
    //         // 2) 장바구니로 이동
    //         window.location.href = "/home/cart";
    //     });
    // });

    /* ============================================================
       7. 지역 선택 변경 시 세션에 저장
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
       8. 주문하기 버튼 (로그인 + 매장 선택 체크)
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
       9. 초기 알림 상태 점검
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

    if (couponEl) {
        couponEl.addEventListener("click", () => {
            if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
                alert("로그인이 필요합니다.");
                return;
            }
            location.href = "/home/coupon";  // 쿠폰 페이지 이동
        });
    }
});