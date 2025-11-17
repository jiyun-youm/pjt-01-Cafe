document.addEventListener('DOMContentLoaded', function() {

    // 가격 입력 자동 포맷팅
    const menuPriceInput = document.getElementById('menuPrice');

    function formatPriceInput(event) {
        let value = event.target.value.replace(/[^0-9]/g, '');
        if (value) {
            event.target.value = Number(value).toLocaleString('ko-KR') + '원';
        } else {
            event.target.value = '';
        }
    }

    if (menuPriceInput) {
        menuPriceInput.addEventListener('input', formatPriceInput);
    }

    // 개별 삭제
    document.querySelector('.menu-table').addEventListener('click', function(e) {
        if (e.target.classList.contains('delete-btn')) {
            const id = e.target.dataset.id;

            if (confirm("정말 삭제하시겠습니까?")) {
                fetch(`/admin/deleteMenu/${id}`, {
                    method: 'DELETE'
                }).then(res => {
                    if (res.ok) {
                        alert("삭제 완료되었습니다.");
                        location.reload(); // 새로고침 (DB 최신 반영)
                    }
                });
            }
        }
    });

    // 선택 삭제
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
});
