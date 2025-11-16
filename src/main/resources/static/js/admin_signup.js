// 아이디 중복 확인
function checkDuplicate() {
    let idInput = document.getElementById('id');
    let message = document.getElementById('checkMessage');
    let idValue = idInput.value.trim();

    if (!idValue) {
        message.style.color = 'red';
        message.textContent = '아이디를 입력해주세요.';
        return;
    }

    // 서버에 AJAX 요청
    fetch(`/admin/checkId?id=${encodeURIComponent(idValue)}`)
        .then(response => response.text())
        .then(data => {
            if (data === 'duplicate') {
                message.style.color = 'red';
                message.textContent = '이미 사용 중인 아이디입니다.';
            } else {
                message.style.color = 'green';
                message.textContent = '사용 가능한 아이디입니다!';
            }
        })
        .catch(err => {
            console.error(err);
            message.style.color = 'red';
            message.textContent = '서버 오류 발생';
        });
}
