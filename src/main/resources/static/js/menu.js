// DB에서 찜한 메뉴 목록 로드 → 하트 상태 반영
document.addEventListener("DOMContentLoaded", async () => {
    let likedMenus = await loadLikedMenus();
    updateLikeButtons(likedMenus);

    // 메뉴 링크 클릭 시 하트 버튼 클릭으로 인한 페이지 이동 방지
    document.querySelectorAll(".menu-link").forEach(link => {
        link.addEventListener("click", (e) => {
            if (e.target.classList.contains("like-button")) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });
});

// DB에서 찜 목록 가져오기
async function loadLikedMenus() {
    try {
        let response = await fetch("/like/list", {
            method: "GET",
            credentials: "include" // 로그인 세션 유지
        });

        if (!response.ok)
            return [];

        return await response.json();
    } catch (err) {
        console.error("찜 목록 불러오기 실패:", err);
        return [];
    }
}

// 하트 표시 적용
function updateLikeButtons(likedMenus) {
    let likeButtons = document.querySelectorAll(".like-button");

    likeButtons.forEach((btn) => {
        let menuItem = btn.closest(".menu-item");
        let id = menuItem.dataset.menuId;

        // DB에 이미 찜한 메뉴이면 하트 표시 변경
        btn.textContent = likedMenus.some(item => item.menuId == id)
            ? "❤"
            : "♡";

        // 클릭 이벤트 설정
        btn.onclick = (event) => toggleLike(btn, event);
    });
}

// 찜 토글 및 DB 반영
async function toggleLike(element, event) {
    event.preventDefault();
    event.stopPropagation();

    let menuItem = element.closest(".menu-item");
    let menuId = menuItem.dataset.menuId;

    try {
        let response = await fetch("/like/toggle", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `menuId=${menuId}`,
            credentials: "include"
        });

        let result = await response.json();

        if (!result) {
            console.error("찜 반영 실패");
            return;
        }

        // UI 아이콘만 즉시 변경
        element.textContent = (element.textContent === "❤") ? "♡" : "❤";

    } catch (err) {
        console.error("찜 토글 오류:", err);
    }
}
