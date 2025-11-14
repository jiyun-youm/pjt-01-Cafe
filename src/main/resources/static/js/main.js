document.addEventListener('DOMContentLoaded', () => {

    // --- 모달 요소 가져오기 ---
    let loginModalOverlay = document.getElementById('login-modal-overlay');
    let signupModalOverlay = document.getElementById('signup-modal-overlay');

    let loginModalTrigger = document.getElementById('login-modal-trigger');
    let switchToSignupTrigger = document.getElementById('switch-to-signup-trigger');

    let loginModalClose = document.getElementById('login-modal-close');
    let signupModalClose = document.getElementById('signup-modal-close');

    // --- 폼 요소 가져오기 ---
    let loginForm = document.getElementById('modalLoginForm');
    let signupForm = document.getElementById('modalSignupForm');

    // --- 알림 팝업 요소 ---
    let notificationTrigger = document.getElementById('notification-trigger');
    let notificationPopup = document.getElementById('notification-popup');
    
    // --- 이메일 중복 확인 버튼 ---
    let checkEmailButton = document.getElementById('check-email-button');
    let signupEmailInput = document.getElementById('signup-email');

    // 알림 아이콘 클릭 시
    if (notificationTrigger) {
        notificationTrigger.addEventListener('click', (e) => {
            e.preventDefault();
            notificationPopup.classList.toggle('show');
        });
    }

    // 문서 다른 곳 클릭 시 알림 팝업 닫기
    document.addEventListener('click', (e) => {
        if (notificationPopup && !notificationTrigger.contains(e.target) && !notificationPopup.contains(e.target)) {
            notificationPopup.classList.remove('show');
        }
    });

    // --- 비로그인 시 (로그인 모달 관련) ---
    if (!IS_LOGGED_IN) {

        // '로그인' 탭 클릭 -> 로그인 모달 열기
        if (loginModalTrigger) {
            loginModalTrigger.addEventListener('click', (e) => {
                e.preventDefault();
                loginModalOverlay.classList.add('show');
            });
        }

        // 로그인 모달 'Sign Up' 버튼 클릭 -> 회원가입 모달 열기
        if (switchToSignupTrigger) {
            switchToSignupTrigger.addEventListener('click', () => {
                loginModalOverlay.classList.remove('show');
                signupModalOverlay.classList.add('show');
                clearErrorMessages(loginForm); // 폼 에러 초기화
            });
        }

        // 로그인 모달 닫기 버튼
        if (loginModalClose) {
            loginModalClose.addEventListener('click', () => {
                loginModalOverlay.classList.remove('show');
                clearErrorMessages(loginForm); // 폼 에러 초기화
            });
        }

        // 회원가입 모달 닫기 버튼
        if (signupModalClose) {
            signupModalClose.addEventListener('click', () => {
                signupModalOverlay.classList.remove('show');
                clearErrorMessages(signupForm); // 폼 에러 초기화
            });
        }

        if (checkEmailButton) {
            checkEmailButton.addEventListener('click', async () => {
                const email = signupEmailInput.value;

                clearErrorMessages(signupForm, 'email');
                clearSuccessMessages(signupForm, 'email');

                if (!email) {
                    displayErrorMessage(signupForm, 'email', '이메일을 적어주세요.');
                    return;
                }

                if (!email.includes('@')) {
                    // 형식이 틀렸을 때
                    displayErrorMessage(signupForm, 'email', '올바른 이메일 주소를 입력해주세요.');
                    return;
                }

                // 3. API 호출 (이후 로직은 동일)
                try {
                    const response = await fetch(`/api/member/check-email?email=${encodeURIComponent(email)}`);
                    const result = await response.json();

                    if (response.ok) {
                        // 사용 가능
                        displaySuccessMessage(signupForm, 'email', result.message);
                    } else {
                        // 중복 (사용 불가)
                        displayErrorMessage(signupForm, 'email', result.message);
                    }
                } catch (error) {
                    console.error('이메일 중복 확인 실패:', error);
                    displayErrorMessage(signupForm, 'email', '확인 중 오류가 발생했습니다.');
                }
            });
        }

        // [추가] 이메일 입력창 내용 변경 시, 중복확인 메시지 초기화
        if (signupEmailInput) {
            signupEmailInput.addEventListener('input', () => {
                clearErrorMessages(signupForm, 'email');
                clearSuccessMessages(signupForm, 'email');
            });
        }


        // 회원가입 폼 제출 (AJAX)
        if (signupForm) {
            signupForm.addEventListener('submit', async (e) => {
                e.preventDefault();

                // 기존 에러/성공 메시지 모두 삭제
                clearErrorMessages(signupForm);
                clearSuccessMessages(signupForm);

                // 폼 데이터를 JSON
                const formData = new FormData(signupForm);
                const data = Object.fromEntries(formData.entries());

                // 프론트엔드 유효성 검사 (빈 값 체크)
                let isValid = true; // 유효성 플래그

                if (!data.email) {
                    displayErrorMessage(signupForm, 'email', '이메일은 필수입니다.');
                    isValid = false;
                }
                if (!data.username) {
                    displayErrorMessage(signupForm, 'username', '이름(닉네임)은 필수입니다.');
                    isValid = false;
                }
                if (!data.password) {
                    displayErrorMessage(signupForm, 'password', '비밀번호는 필수입니다.');
                    isValid = false;
                }
                if (!data.passwordCheck) {
                    displayErrorMessage(signupForm, 'passwordCheck', '비밀번호 확인은 필수입니다.');
                    isValid = false;
                }

                // 비밀번호 일치 여부도 미리 검사 (선택사항이지만 권장)
                if (data.password && data.passwordCheck && data.password !== data.passwordCheck) {
                    displayErrorMessage(signupForm, 'passwordCheck', '비밀번호가 일치하지 않습니다.');
                    isValid = false;
                }

                // 4. 유효성 검사에 실패하면 서버 전송 차단
                if (!isValid) {
                    return; // fetch 요청을 보내지 않고 함수 종료
                }

                // 5. 서버로 fetch 요청 (유효성 검사를 통과한 경우에만 실행)
                try {
                    const response = await fetch('/api/member/signup', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(data)
                    });

                    const result = await response.json();

                    if (response.ok) {
                        alert(result.message);
                        signupModalOverlay.classList.remove('show');
                        loginModalOverlay.classList.add('show');
                        signupForm.reset();

                    } else {
                        displayErrorMessage(signupForm, result.field, result.message);
                    }
                } catch (error) {
                    console.error('회원가입 요청 실패:', error);
                    displayErrorMessage(signupForm, 'username', '요청 중 오류가 발생했습니다.'); // username은 예시
                }
            });
        }

        // 로그인 폼 제출 (AJAX)
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                clearErrorMessages(loginForm);

                let formData = new FormData(loginForm);
                let data = Object.fromEntries(formData.entries());

                try {
                    let response = await fetch('/api/member/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(data)
                    });

                    let result = await response.json();

                    if (response.ok) {
                        // 로그인 성공
                        alert(result.message);
                        window.location.reload(); // 페이지 새로고침
                    } else {
                        // 로그인 실패
                        displayErrorMessage(loginForm, 'password', result.message); // 비밀번호 필드 아래에 에러 표시
                    }
                } catch (error) {
                    console.error('로그인 요청 실패:', error);
                    displayErrorMessage(loginForm, 'password', '로그인 중 오류 발생');
                }
            });
        }

    }

    // --- 로그인 여부와 관계없이 'login-required' 링크 처리 ---
    let loginRequiredLinks = document.querySelectorAll('.login-required');
    loginRequiredLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            if (!IS_LOGGED_IN) {
                e.preventDefault(); // 페이지 이동 방지
                alert('로그인이 필요한 서비스입니다.');
                if (loginModalOverlay) {
                    loginModalOverlay.classList.add('show'); // 로그인 모달 열기
                }
            }
        });
    });

});

/* 폼 내부의 모든 .error-message 내용을 삭제 */
function clearErrorMessages(formElement) {
    formElement.querySelectorAll('.error-message').forEach(el => {
        el.textContent = '';
    });
}
/* 폼 내부의 .success-message 내용을 삭제 */
function clearSuccessMessages(formElement, fieldName) {
    const selector = fieldName
        ? `.success-message[data-field="${fieldName}"]`
        : '.success-message';
    formElement.querySelectorAll(selector).forEach(el => {
        el.textContent = '';
    });
}

/* 특정 필드 아래에 에러 메시지를 표시 */
function displayErrorMessage(formElement, fieldName, message) {
    clearSuccessMessages(formElement, fieldName);
    let errorEl = formElement.querySelector(`.error-message[data-field="${fieldName}"]`);
    if (!errorEl) {
        errorEl = formElement.querySelector('.error-message[data-field="unknown"]');
        if (!errorEl) {
            errorEl = formElement.querySelector('.error-message[data-field="username"]');
        }
    }
    if (errorEl) {
        errorEl.textContent = message;
    } else {
        alert(message);
    }
}

/* 특정 필드 아래에 성공 메시지를 표시 */
function displaySuccessMessage(formElement, fieldName, message) {
    clearErrorMessages(formElement, fieldName);

    let successEl = formElement.querySelector(`.success-message[data-field="${fieldName}"]`);
    if (successEl) {
        successEl.textContent = message;
    } else {
        alert(message);
    }
}