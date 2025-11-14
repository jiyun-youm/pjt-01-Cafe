const LIKE_KEY = "likedMenus";

document.addEventListener("DOMContentLoaded", () => {
    const likedMenus = JSON.parse(localStorage.getItem(LIKE_KEY)) || [];
    const likeButtons = document.querySelectorAll(".menu-item .like-button");

    // 기존 좋아요 상태 표시
    likeButtons.forEach((btn) => {
        const menuItem = btn.closest(".menu-item");
        const menuName = menuItem.querySelector(".menu-name").textContent.trim();
        const isLiked = likedMenus.some(item => item.name === menuName); // ✅ 이름 기준으로 비교

        btn.textContent = isLiked ? "❤" : "♡";
    });
});

// 메뉴 상세 정보를 추출
function getMenuDetails(element) {
    const menuItem = element.closest(".menu-item");
    const name = menuItem.querySelector(".menu-name").textContent.trim();

    const priceText = menuItem.querySelector(".menu-price").dataset.price ||
        menuItem.querySelector(".menu-price").textContent.replace('원', '').trim();
    const price = parseInt(priceText, 10);

    const image = menuItem.querySelector(".menu-image").getAttribute('src');
    const menuId = menuItem.dataset.menuId; // 있어도 무관함

    return { menuId, name, price, image, temp: 'ICE/HOT' };
}

// 하트 토글
function toggleLike(element, event) {
    event.stopPropagation();

    const menuDetails = getMenuDetails(element);
    let likedMenus = JSON.parse(localStorage.getItem(LIKE_KEY)) || [];

    // ✅ 이름 기준으로 비교
    const isLiked = likedMenus.some(item => item.name === menuDetails.name);

    // UI 즉시 변경
    element.textContent = isLiked ? "♡" : "❤";

    // localStorage 갱신
    if (isLiked) {
        likedMenus = likedMenus.filter(item => item.name !== menuDetails.name);
        console.log(menuDetails.name + " 좋아요 해제");
    } else {
        likedMenus.push(menuDetails);
        console.log(menuDetails.name + " 좋아요 설정");
    }

    localStorage.setItem(LIKE_KEY, JSON.stringify(likedMenus));
}

    // // 메뉴 클릭 이벤트
    // function selectMenu(menuItemElement) {
    //     const menuId = menuItemElement.getAttribute("data-menu-id");
    //     // 메뉴 상세 페이지로 이동
    //     window.location.href = `/home/order_detail`;
    // }

function selectMenu(element) {
    const menuId = element.getAttribute('data-menu-id');

    console.log(menuId)

    window.location.href = `/home/order_detail?id=${menuId}`;
}

