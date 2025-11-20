document.addEventListener('DOMContentLoaded', function() {

    /** ---------------- 가격 입력 자동 포맷 ---------------- */
    const menuPriceInput = document.getElementById('menuPrice');

    function getRawPrice() {
        return menuPriceInput.value.replace(/[^0-9]/g, '');
    }

    // 입력 중에는 숫자만 유지 (원 붙이지 않음)
    if (menuPriceInput) {
        menuPriceInput.addEventListener('input', function() {
            let value = getRawPrice();
            menuPriceInput.value = value;
        });

        // 포커스 벗어날 때만 포맷 적용
        menuPriceInput.addEventListener('blur', function() {
            let value = getRawPrice();
            if (value) {
                menuPriceInput.value = Number(value).toLocaleString('ko-KR') + '원';
            } else {
                menuPriceInput.value = '';
            }
        });
    }

    /** ---------------- 폼 제출 시 원본값 전달 ---------------- */
    const newMenuForm = document.getElementById("newMenuForm");

    if (newMenuForm) {
        let hiddenPriceInput = document.createElement("input");
        hiddenPriceInput.type = "hidden";
        hiddenPriceInput.name = "menuPrice";
        newMenuForm.appendChild(hiddenPriceInput);

        newMenuForm.addEventListener("submit", function(e) {
            const rawValue = getRawPrice();
            hiddenPriceInput.value = rawValue;
            menuPriceInput.disabled = true;
        });
    }


    /** ---------------- 상세 토글 ---------------- */
    document.querySelectorAll(".menu-row").forEach(row => {
        row.addEventListener("click", (e) => {
            if (e.target.closest("td")?.cellIndex === 0) return;
            if (e.target.classList.contains("delete-btn")) return;

            const detailRow = row.nextElementSibling;
            if (!detailRow) return;

            detailRow.style.display = detailRow.style.display === "table-row" ? "none" : "table-row";
        });
    });


    /** ---------------- 판매 상태 저장 ---------------- */
    document.querySelectorAll(".status-save-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            let detailRow = this.closest(".detail-row");
            let menuRow = detailRow.previousElementSibling;
            let menuId = menuRow.dataset.menuId;
            let selectedStatus = detailRow.querySelector(".status-select").value;

            fetch("/admin/updateStatus", {
                method: "POST",
                headers: {"Content-Type":"application/json"},
                body: JSON.stringify({menuId: menuId, status: selectedStatus})
            }).then(res => res.text()).then(result => {
                if (result === "success") {
                    alert("상태가 변경되었습니다");
                }
            });
        });
    });


    /** ---------------- 개별 삭제 ---------------- */
    document.querySelector('.menu-table').addEventListener('click', function(e) {
        if (e.target.classList.contains('delete-btn')) {
            e.stopPropagation();
            const id = e.target.dataset.id;

            if (confirm("정말 삭제하시겠습니까?")) {
                fetch(`/admin/deleteMenu/${id}`, {
                    method: 'DELETE'
                }).then(res => {
                    if (res.ok) {
                        alert("삭제 완료되었습니다.");
                        location.reload();
                    }
                });
            }
        }
    });


    /** ---------------- 선택 삭제 ---------------- */
    document.querySelector('.select-delete-btn').addEventListener('click', function () {
        const checked = document.querySelectorAll('tbody input[type="checkbox"]:checked');

        if (checked.length === 0) {
            alert("삭제할 메뉴를 선택해주세요.");
            return;
        }

        if (!confirm(`${checked.length}개의 메뉴를 삭제하시겠습니까?`)) return;

        const ids = Array.from(checked).map(item => item.value);

        fetch(`/admin/deleteMenuBatch`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(ids)
        }).then(res => {
            if (res.ok) {
                alert("선택한 메뉴가 삭제되었습니다.");
                location.reload();
            }
        });
    });


    /** ---------------- 이미지 미리보기 ---------------- */
    let fileInput = document.getElementById('menuImage');
    let imageBox = document.querySelector('.image-upload-box');

    if (fileInput) {
        fileInput.addEventListener('change', (e) => {
            let file = e.target.files[0];
            if (file) {
                let img = document.createElement('img');
                img.src = URL.createObjectURL(file);
                img.style.width = "100%";
                img.style.height = "100%";
                img.style.objectFit = "cover";
                imageBox.innerHTML = "";
                imageBox.appendChild(img);
            }
        });
    }


    /** ---------------- 정렬 기능 ---------------- */
    const table = document.querySelector(".menu-table");
    const headers = table.querySelectorAll("thead th");
    let sortStatus = {};

    headers.forEach((header, idx) => {
        if (idx === 4) return;
        header.style.cursor = "pointer";
        header.addEventListener("click", () => sortTable(idx));
    });

    function sortTable(colIndex) {
        const tbody = table.querySelector("tbody");
        const rows = Array.from(tbody.querySelectorAll("tr")).filter((_, i) => i % 2 === 0);

        const isAsc = sortStatus[colIndex] = !sortStatus[colIndex];

        rows.sort((a, b) => {
            let A = a.children[colIndex].innerText.replace(/원|,/g, '').trim();
            let B = b.children[colIndex].innerText.replace(/원|,/g, '').trim();

            if (!isNaN(A) && !isNaN(B)) {
                return isAsc ? A - B : B - A;
            }
            return isAsc ? A.localeCompare(B, "ko-KR") : B.localeCompare(A, "ko-KR");
        });

        rows.forEach(row => {
            let detailRow = row.nextElementSibling;
            tbody.appendChild(row);
            tbody.appendChild(detailRow);
        });
    }


    /** ---------------- 수정 버튼 이동 ---------------- */
    document.querySelectorAll(".edit-menu-btn").forEach(btn => {
        btn.addEventListener("click", function(e) {
            e.stopPropagation();
            const menuId = this.dataset.id;
            window.location.href = `/admin/updateMenu/${menuId}`;
        });
    });

});
