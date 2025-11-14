document.addEventListener("DOMContentLoaded", () => {
    const idInput = document.getElementById("id");
    const passwordInput = document.getElementById("password");
    const form = document.querySelector("form");

    form.addEventListener("submit", (e) => {
        e.preventDefault(); // 기본 제출 방지

        const idValue = idInput.value.trim();
        const passwordValue = passwordInput.value.trim();

        if (!idValue || !passwordValue) {
            alert("아이디와 비밀번호를 모두 입력해주세요.");
            return;
        }

        // 아이디/비밀번호 상관없이 로그인 성공
        alert("로그인 성공!");
        window.location.href = "/admin/orders"; // 관리자 페이지로 이동
    });
});
