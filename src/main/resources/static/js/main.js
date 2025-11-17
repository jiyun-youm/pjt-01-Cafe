document.addEventListener('DOMContentLoaded', () => {

    /* ============================================================
       🚀 1. 소셜 로그인 에러 처리 (중복 실행 방지 & 메시지 디코딩)
    ============================================================ */
    const params = new URLSearchParams(window.location.search);
    const oauthError = params.get('oauthError');

    // 에러 메시지가 있고, 아직 알림을 띄운 적이 없을 때만 실행
    if (oauthError && !window.oauthErrorShown) {

        // (1) 플래그 설정 (중복 실행 방지)
        window.oauthErrorShown = true;

        // (2) 메시지 디코딩 (+ 기호를 공백으로 변환)
        let message = decodeURIComponent(oauthError).replace(/\+/g, ' ');

        // (3) URL 정리 (알림창 띄우기 전에 주소창을 깨끗하게 만듦)
        const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
        window.history.replaceState({ path: cleanUrl }, '', cleanUrl);

        // (4) 사용자에게 알림
        alert(message);
    }


    /* ============================================================
       🏛️ 2. DOM 요소 선택
    ============================================================ */
    let loginModalOverlay = document.getElementById('login-modal-overlay');
    let signupModalOverlay = document.getElementById('signup-modal-overlay');

    let loginModalTrigger = document.getElementById('login-modal-trigger');
    let switchToSignupTrigger = document.getElementById('switch-to-signup-trigger');
    let loginModalClose = document.getElementById('login-modal-close');
    let signupModalClose = document.getElementById('signup-modal-close');

    let loginForm = document.getElementById('modalLoginForm');
    let signupForm = document.getElementById('modalSignupForm');

    let notificationTrigger = document.getElementById('notification-trigger');
    let notificationPopup = document.getElementById('notification-popup');

    let checkEmailButton = document.getElementById('check-email-button');
    let signupEmailInput = document.getElementById('signup-email');

    let userRegion = document.getElementById('userRegion');
    let orderBtn = document.getElementById('orderBtn');


    /* ===========================
       🔔 알림 팝업 (헤더 종모양)
    ============================*/
    if (notificationTrigger) {
        notificationTrigger.addEventListener('click', (e) => {
            e.preventDefault();
            notificationPopup.classList.toggle('show');
        });
    }

    document.addEventListener('click', (e) => {
        if (notificationPopup &&
            !notificationTrigger.contains(e.target) &&
            !notificationPopup.contains(e.target)) {
            notificationPopup.classList.remove('show');
        }
    });

    async function initRegionSelect() {
        if (!userRegion) return;

        try {
            const resp = await fetch("/home/getRegion");
            const storeName = await resp.text();

            if (storeName && storeName !== "null") {
                userRegion.value = storeName;
            } else {
                userRegion.value = "selecting";
            }
        } catch (e) {
            console.error("getRegion error:", e);
        }
    }

    initRegionSelect(); // 실행


    /* ============================================================
       2. 지역 선택 시 세션에 저장 또는 삭제
    ============================================================ */
    if (userRegion) {
        userRegion.addEventListener("change", () => {
            const region = userRegion.value;

            fetch("/home/saveRegion", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ region })
            }).catch(err => console.error(err));
        });
    }


    /* ============================================================
       3. 주문하기 버튼 (로그인 확인 + 지점 확인)
    ============================================================ */

    async function moveToMenuBySession() {
        try {
            const resp = await fetch("/home/getRegion");
            const region = await resp.text();

            if (!region || region === "null" || region === "selecting") {
                alert("지점을 선택해주세요.");
                return;
            }

            window.location.href = "/menu/coffee";
        } catch (e) {
            console.error("moveToMenu error:", e);
        }
    }

    if (orderBtn) {
        orderBtn.addEventListener("click", async (e) => {
            e.preventDefault();

            // 1) 로그인 여부 확인
            if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
                const loginModalOverlay = document.getElementById("login-modal-overlay");
                if (loginModalOverlay) loginModalOverlay.classList.add("show");
                return;
            }

            // 2) 로그인 시 지점 확인 후 이동
            await moveToMenuBySession();
        });
    }




    /* ===========================
       🔐 로그인/회원가입 모달 로직
       (로그인 상태가 아닐 때만 동작)
    ============================*/
    if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {

        // 로그인 버튼 클릭
        if (loginModalTrigger) {
            loginModalTrigger.addEventListener('click', (e) => {
                e.preventDefault();
                loginModalOverlay.classList.add('show');
            });
        }

        // 로그인 -> 회원가입 전환
        if (switchToSignupTrigger) {
            switchToSignupTrigger.addEventListener('click', () => {
                loginModalOverlay.classList.remove('show');
                signupModalOverlay.classList.add('show');
                clearErrorMessages(signupForm);
            });
        }

        // 닫기 버튼들
        if (loginModalClose) {
            loginModalClose.addEventListener('click', () => {
                loginModalOverlay.classList.remove('show');
                clearErrorMessages(loginForm);
            });
        }

        if (signupModalClose) {
            signupModalClose.addEventListener('click', () => {
                signupModalOverlay.classList.remove('show');
                clearErrorMessages(signupForm);
            });
        }

        /* 이메일 중복확인 */
        if (checkEmailButton) {
            checkEmailButton.addEventListener('click', async () => {
                const email = signupEmailInput.value;
                clearErrorMessages(signupForm, 'email');
                clearSuccessMessages(signupForm, 'email');

                if (!email) {
                    displayErrorMessage(signupForm, 'email', '이메일을 입력하세요.');
                    return;
                }
                if (!email.includes('@')) {
                    displayErrorMessage(signupForm, 'email', '올바른 이메일 형식이 아닙니다.');
                    return;
                }

                try {
                    const response = await fetch(`/api/member/check-email?email=${encodeURIComponent(email)}`);
                    const result = await response.json();

                    if (response.ok) {
                        displaySuccessMessage(signupForm, 'email', result.message);
                    } else {
                        displayErrorMessage(signupForm, 'email', result.message);
                    }
                } catch (error) {
                    console.error(error);
                    displayErrorMessage(signupForm, 'email', '중복확인 중 오류 발생');
                }
            });
        }

        // 이메일 입력 시 메시지 초기화
        if (signupEmailInput) {
            signupEmailInput.addEventListener('input', () => {
                clearErrorMessages(signupForm, 'email');
                clearSuccessMessages(signupForm, 'email');
            });
        }

        /* 비밀번호 일치 검사 */
        const passwordInput = document.getElementById('signup-password');
        const passwordCheckInput = document.getElementById('signup-password-check');

        function validatePasswords() {
            if (passwordInput.value && passwordCheckInput.value) {
                if (passwordInput.value !== passwordCheckInput.value) {
                    clearSuccessMessages(signupForm, 'passwordCheck');
                    displayErrorMessage(signupForm, 'passwordCheck', '비밀번호가 서로 일치하지 않습니다.');
                } else {
                    clearErrorMessages(signupForm, 'passwordCheck');
                    displaySuccessMessage(signupForm, 'passwordCheck', '비밀번호가 일치합니다.');
                }
            } else {
                clearErrorMessages(signupForm, 'passwordCheck');
                clearSuccessMessages(signupForm, 'passwordCheck');
            }
        }

        if (passwordInput) passwordInput.addEventListener('input', validatePasswords);
        if (passwordCheckInput) passwordCheckInput.addEventListener('input', validatePasswords);

        /* 회원가입 폼 제출 */
        if (signupForm) {
            signupForm.addEventListener('submit', async (e) => {
                e.preventDefault();

                signupForm.querySelectorAll('.error-message').forEach(el => {
                    if (el.dataset.field !== 'email') el.textContent = '';
                });

                const formData = new FormData(signupForm);
                const data = Object.fromEntries(formData.entries());
                let ok = true;

                if (!data.email) { displayErrorMessage(signupForm, 'email', '이메일 필수'); ok = false; }
                if (!data.username) { displayErrorMessage(signupForm, 'username', '닉네임 필수'); ok = false; }
                if (!data.password) { displayErrorMessage(signupForm, 'password', '비밀번호 필수'); ok = false; }
                if (data.password !== data.passwordCheck) { displayErrorMessage(signupForm, 'passwordCheck', '비밀번호 불일치'); ok = false; }

                if (!ok) return;

                try {
                    const response = await fetch('/api/member/signup', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(data)
                    });
                    const result = await response.json();

                    if (response.ok) {
                        alert(result.message);
                        signupModalOverlay.classList.remove('show');
                        loginModalOverlay.classList.add('show');
                        signupForm.reset();
                        clearErrorMessages(signupForm);
                        clearSuccessMessages(signupForm);
                    } else {
                        displayErrorMessage(signupForm, result.field || 'username', result.message);
                    }
                } catch (error) {
                    console.error(error);
                    displayErrorMessage(signupForm, 'username', '회원가입 오류');
                }
            });
        }
    }

    /* 로그인 권한 보호 링크 */
    let loginRequiredLinks = document.querySelectorAll('.login-required');
    loginRequiredLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
                e.preventDefault();
                if (loginModalOverlay) loginModalOverlay.classList.add('show');
            }
        });
    });

    /* ===========================
       🔥 일반 로그인 실패 시 모달 자동 열기
       (?error 파라미터 존재 시)
    ============================*/
    // oauthError가 없을 때만 작동
    if (!oauthError && params.has("error")) {
        if (loginModalOverlay) loginModalOverlay.classList.add("show");
    }

});

/* -----------------------------
   🔧 공용 메시지 헬퍼 함수들
------------------------------ */
function clearErrorMessages(formElement, field = null) {
    if (field) {
        let target = formElement.querySelector(`.error-message[data-field="${field}"]`);
        if (target) target.textContent = '';
    } else {
        formElement.querySelectorAll('.error-message').forEach(el => el.textContent = '');
    }
}
function clearSuccessMessages(formElement, field = null) {
    if (field) {
        let target = formElement.querySelector(`.success-message[data-field="${field}"]`);
        if (target) target.textContent = '';
    } else {
        formElement.querySelectorAll('.success-message').forEach(el => el.textContent = '');
    }
}
function displayErrorMessage(formElement, field, message) {
    let target = formElement.querySelector(`.error-message[data-field="${field}"]`);
    if (target) target.textContent = message;
}
function displaySuccessMessage(formElement, field, message) {
    let target = formElement.querySelector(`.success-message[data-field="${field}"]`);
    if (target) target.textContent = message;
}
function setVh() {
    document.documentElement.style.setProperty('--vh', window.innerHeight * 0.01 + 'px');
}
setVh();
window.addEventListener('resize', setVh);