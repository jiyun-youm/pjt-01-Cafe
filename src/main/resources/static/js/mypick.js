async function cancelLike(element) {
    let menuId = element.closest(".menu-item").dataset.menuId;

    let result = await fetch("/like/toggle", {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: `menuId=${menuId}`
    }).then(res => res.json());

    if (!result) {
        element.closest(".menu-item").remove();
    }

    // 목록 비었는지 체크
    let remainItems = document.querySelectorAll(".menu-item");

    if (remainItems.length === 0) {
        let container = document.getElementById("myPickList");
        container.innerHTML = `
            <div style="text-align:center; padding:50px; color:#777;">
                찜한 메뉴가 없습니다 ☕
            </div>
        `;
    }
}
