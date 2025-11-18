// DB에서 찜한 메뉴 불러오기 → UI 반영
document.addEventListener("DOMContentLoaded", async () => {
    const likedMenus = await loadLikedMenus();
    updateLikeButtons(likedMenus);

    // 하트 클릭 시 메뉴 상세 이동 방지
    document.querySelectorAll(".menu-link").forEach(link => {
        link.addEventListener("click", (e) => {
            if (e.target.classList.contains("like-button")) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });
});

// 찜 목록 가져오기
async function loadLikedMenus() {
    try {
        const res = await fetch("/like/list", { method: "GET", credentials: "include" });
        if (!res.ok) return [];
        return await res.json();
    } catch (err) {
        console.error("찜 목록 불러오기 실패:", err);
        return [];
    }
}

// UI 하트 초기 세팅
function updateLikeButtons(likedMenus) {
    document.querySelectorAll(".like-button").forEach(btn => {
        const menuId = btn.dataset.menuId;
        btn.textContent = likedMenus.some(item => item.menuId == menuId) ? "❤" : "♡";

        btn.onclick = (event) => toggleLike(btn, event);
    });
}

// 찜 토글 처리
async function toggleLike(element, event) {
    event.preventDefault();
    event.stopPropagation();

    const menuId = element.dataset.menuId;
    console.log("토글 실행 → menuId:", menuId);

    if (!menuId) {
        console.error("menuId 누락 → HTML data-menu-id 확인 필요");
        return;
    }

    try {
        const response = await fetch("/like/toggle", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `menuId=${menuId}`,
            credentials: "include"
        });

        const result = await response.json();

        element.textContent = result ? "❤" : "♡"; // DB 반영 결과 기준

    } catch (err) {
        console.error("찜 토글 오류:", err);
    }
}
