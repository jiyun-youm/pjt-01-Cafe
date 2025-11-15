document.addEventListener('DOMContentLoaded', () => {

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

    /* ===========================
       🔔 알림 팝업
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


    /* ===========================
       🔐 로그인 되어있지 않으면 모달 열기
    ============================*/
    if (!IS_LOGGED_IN) {

        // 로그인 버튼 클릭 → 모달 열기
        if (loginModalTrigger) {
            loginModalTrigger.addEventListener('click', (e) => {
                e.preventDefault();
                loginModalOverlay.classList.add('show');
            });
        }

        // 로그인 모달 → 회원가입 모달 전환
        if (switchToSignupTrigger) {
            switchToSignupTrigger.addEventListener('click', () => {
                loginModalOverlay.classList.remove('show');
                signupModalOverlay.classList.add('show');
                clearErrorMessages(signupForm);
            });
        }

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

        /* ===========================
           📧 이메일 중복확인
        ============================*/
        if (checkEmailButton) {
            checkEmailButton.addEventListener('click', async () => {

                const email = signupEmailInput.value;

                // 헬퍼 함수 이름 오타 수정 (clearSuccessMessages)
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

        // ⬇️⬇️⬇️ [수정됨] 이 블록이 새로 추가되었습니다 ⬇️⬇️⬇️
        /* ===========================
           🔑 실시간 비밀번호 일치 검사
        ============================*/
        // 1. 회원가입 폼에서 비밀번호 관련 요소들을 선택합니다.
        const passwordInput = document.getElementById('signup-password');
        const passwordCheckInput = document.getElementById('signup-password-check');

        // 2. 실시간 비밀번호 일치 검사 함수
        function validatePasswords() {
            // '비밀번호' 또는 '비밀번호 확인' 둘 다 값이 있을 때만 비교 시작
            if (passwordInput.value && passwordCheckInput.value) {

                if (passwordInput.value !== passwordCheckInput.value) {
                    // 1. 일치하지 않을 때:
                    clearSuccessMessages(signupForm, 'passwordCheck'); // ⬅️ 성공 메시지를 지우고
                    displayErrorMessage(signupForm, 'passwordCheck', '비밀번호가 서로 일치하지 않습니다.'); // ⬅️ 에러 메시지를 띄움
                } else {
                    // 2. 일치할 때: (🔥 수정된 부분)
                    clearErrorMessages(signupForm, 'passwordCheck'); // ⬅️ 에러 메시지를 지우고
                    displaySuccessMessage(signupForm, 'passwordCheck', '비밀번호가 일치합니다.'); // ⬅️ 성공 메시지를 띄움
                }

            } else {
                // 3. 둘 중 하나라도 비어있을 때:
                clearErrorMessages(signupForm, 'passwordCheck'); // ⬅️ 모든 메시지를 지움
                clearSuccessMessages(signupForm, 'passwordCheck');
            }
        }

        if (passwordInput) {
            passwordInput.addEventListener('input', validatePasswords);
        }
        if (passwordCheckInput) {
            passwordCheckInput.addEventListener('input', validatePasswords);
        }
        /* ===========================
           📝 회원가입 (AJAX 유지)
        ============================*/
        if (signupForm) {
            signupForm.addEventListener('submit', async (e) => {
                e.preventDefault();

                // 폼 제출 시, 이메일 외 모든 에러 메시지를 지웁니다.
                // (이메일은 중복확인 성공 메시지가 남아있어야 하므로)
                signupForm.querySelectorAll('.error-message').forEach(el => {
                    if (el.dataset.field !== 'email') {
                        el.textContent = '';
                    }
                });

                const formData = new FormData(signupForm);
                const data = Object.fromEntries(formData.entries());

                let ok = true;

                // [수정됨] 헬퍼 함수를 사용하도록 통일
                if (!data.email) {
                    displayErrorMessage(signupForm, 'email', '이메일은 필수입니다.');
                    ok = false;
                }
                if (!data.username) {
                    displayErrorMessage(signupForm, 'username', '닉네임은 필수입니다.');
                    ok = false;
                }
                if (!data.password) {
                    displayErrorMessage(signupForm, 'password', '비밀번호는 필수입니다.');
                    ok = false;
                }
                // 이 검사는 실시간으로도 수행되지만, submit 시에도 최종 확인합니다.
                if (data.password !== data.passwordCheck) {
                    displayErrorMessage(signupForm, 'passwordCheck', '비밀번호가 일치하지 않습니다.');
                    ok = false;
                }

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

                        // 회원가입 성공 시 모든 메시지 초기화
                        clearErrorMessages(signupForm);
                        clearSuccessMessages(signupForm);

                    } else {
                        // [수정됨] 서버에서 오는 에러 메시지를 data-field 기반으로 표시
                        // (예: { "field": "email", "message": "이미 가입된 이메일입니다." })
                        displayErrorMessage(signupForm, result.field || 'username', result.message);
                    }

                } catch (error) {
                    console.error(error);
                    displayErrorMessage(signupForm, 'username', '회원가입 오류');
                }
            });
        }
    }


    /* ===========================
       🚧 로그인 안 되어 있을 때 보호 기능
    ============================*/
    let loginRequiredLinks = document.querySelectorAll('.login-required');

    loginRequiredLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            if (!IS_LOGGED_IN) {
                e.preventDefault();
                loginModalOverlay.classList.add('show');
            }
        });
    });


    /* ===========================
       🔥 로그인 실패 시 모달 자동 열기
       ?error 파라미터 존재하면 실행
    ============================*/
    const params = new URLSearchParams(window.location.search);

    if (params.has("error")) {
        if (loginModalOverlay) loginModalOverlay.classList.add("show");
    }

});


/* -----------------------------
   🔧 공용 메시지 헬퍼 함수
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