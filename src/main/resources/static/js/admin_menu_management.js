// menu_management_script.js (모든 변수를 let으로 통일)

document.addEventListener('DOMContentLoaded', function() {
    
    // ------------------------------------------
    // 1. 초기 메뉴 데이터 (let으로 선언)
    // ------------------------------------------
    let menuData = [
        { id: 1, name: "아메리카노", price: 3000, category: "커피" },
        { id: 2, name: "디카페인 아메리카노", price: 3000, category: "커피" },
        { id: 3, name: "카페라떼", price: 3000, category: "커피" },
        { id: 4, name: "복숭아 아이스티", price: 3000, category: "논 커피" }
    ];

    // DOM 요소 참조 (let으로 선언)
    let menuTableBody = document.querySelector('.menu-table tbody');
    let newMenuForm = document.getElementById('newMenuForm');
    let selectDeleteBtn = document.querySelector('.select-delete-btn');
    let menuPriceInput = document.getElementById('menuPrice');

    // ------------------------------------------
    // A. 가격 포맷팅 함수
    // ------------------------------------------
    function formatPriceInput(event) {
        let input = event.target;
        
        // 1. 숫자 외의 문자 (쉼표, '원', 공백 등) 제거
        let rawValue = input.value.replace(/[^0-9]/g, '');
        
        if (rawValue) {
            // 2. 숫자를 천 단위로 쉼표 포맷팅
            let formattedValue = parseInt(rawValue).toLocaleString('ko-KR');
            
            // 3. '원'을 붙여서 필드에 표시
            input.value = `${formattedValue}원`;
        } else {
            input.value = '';
        }
    }

    // ------------------------------------------
    // B. 이벤트 리스너 등록
    // ------------------------------------------
    if (menuPriceInput) {
        menuPriceInput.addEventListener('input', formatPriceInput);
    }

    // ------------------------------------------
    // 2. 메뉴 목록 동적 렌더링 함수
    // ------------------------------------------
    function renderMenuTable() {
        // 기존 내용을 비우기
        menuTableBody.innerHTML = '';

        if (menuData.length === 0) {
            menuTableBody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 30px;">등록된 메뉴가 없습니다.</td></tr>';
            return;
        }

        // 새로운 데이터로 테이블 내용 채우기
        menuData.forEach(menu => {
            let row = menuTableBody.insertRow(); // let 선언
            row.setAttribute('data-menu-id', menu.id);

            // 1. 선택 체크박스
            let cell1 = row.insertCell(); // let 선언
            cell1.innerHTML = `<input type="checkbox" class="menu-select-checkbox" data-menu-id="${menu.id}">`;

            // 2. 메뉴 이름
            row.insertCell().textContent = menu.name;

            // 3. 가격
            row.insertCell().textContent = `${menu.price.toLocaleString('ko-KR')}원`;

            // 4. 카테고리
            row.insertCell().textContent = menu.category;

            // 5. 메뉴 삭제 버튼
            let cell5 = row.insertCell(); // let 선언
            cell5.innerHTML = `<button class="delete-btn" data-menu-id="${menu.id}">×</button>`;
        });
    }

    // ------------------------------------------
    // 3. 새 메뉴 등록 기능
    // ------------------------------------------
    if (newMenuForm) {
        newMenuForm.addEventListener('submit', function(e) {
            e.preventDefault();

            let name = document.getElementById('menuName').value.trim(); // let 선언
            let priceStr = menuPriceInput.value.trim().replace(/[^0-9]/g, ''); // let 선언
            
            let categorySelect = document.getElementById('menuCategory'); // let 선언
            let category = categorySelect.options[categorySelect.selectedIndex].text; // let 선언

            if (!name || !priceStr || category === '카테고리 선택') {
                alert('메뉴 이름, 가격, 카테고리는 필수 입력 항목입니다.');
                return;
            }

            let price = parseInt(priceStr, 10); // let 선언
            if (isNaN(price)) {
                alert('가격이 유효하지 않습니다.');
                return;
            }

            let newId = menuData.length > 0 ? Math.max(...menuData.map(m => m.id)) + 1 : 1; // let 선언
            
            let newMenu = { // let 선언
                id: newId,
                name: name,
                price: price,
                category: category
            };

            menuData.push(newMenu);
            renderMenuTable();
            newMenuForm.reset(); 
            
            alert(`'${name}' 메뉴가 성공적으로 등록되었습니다.`);
        });
    }

    // ------------------------------------------
    // 4. 개별/선택 메뉴 삭제 기능
    // ------------------------------------------
    
    if (menuTableBody) {
        menuTableBody.addEventListener('click', function(e) {
            let target = e.target; // let 선언
            
            if (target.classList.contains('delete-btn')) {
                let menuId = parseInt(target.dataset.menuId); // let 선언
                
                if (confirm('정말로 이 메뉴를 삭제하시겠습니까?')) {
                    menuData = menuData.filter(menu => menu.id !== menuId);
                    renderMenuTable();
                }
            }
        });
    }

    if (selectDeleteBtn) {
        selectDeleteBtn.addEventListener('click', function() {
            // 체크된 체크박스에서 ID를 수집
            let checkedCheckboxes = document.querySelectorAll('.menu-select-checkbox:checked'); // let 선언
            let selectedIds = Array.from(checkedCheckboxes).map(cb => parseInt(cb.dataset.menuId)); // let 선언

            if (selectedIds.length === 0) {
                alert('삭제할 메뉴를 선택해주세요.');
                return;
            }

            if (confirm(`선택된 ${selectedIds.length}개의 메뉴를 삭제하시겠습니까?`)) {
                menuData = menuData.filter(menu => !selectedIds.includes(menu.id));
                renderMenuTable();
            }
        });
    }

    // ------------------------------------------
    // 5. 초기화
    // ------------------------------------------
    renderMenuTable();
});