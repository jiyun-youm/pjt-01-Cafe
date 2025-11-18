let idInput = document.getElementById("id");
let passwordInput = document.getElementById("password");
let loginBtn = document.getElementById("loginBtn");
let signupBtn = document.getElementById("signupBtn");

// loginBtn.addEventListener("click", () => {
//     let idValue = idInput.value.trim();
//     let passwordValue = passwordInput.value.trim();
//
//     if (!idValue || !passwordValue) {
//         alert("아이디와 비밀번호를 모두 입력해주세요.");
//         return;
//     }
//
//     // 로그인 버튼 클릭 시 이동
//     window.location.href = "/admin/orders";
// });

signupBtn.addEventListener("click", () => {
    window.location.href = "/admin/signup";
});
