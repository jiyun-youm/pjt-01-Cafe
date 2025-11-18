
    // DOM
    const segmented = document.querySelector('.segmented');
    const buttons = Array.from(document.querySelectorAll('.segmented-btn'));
    const thumb = document.querySelector('.segmented-thumb');

    // 초기 상태: active 버튼이 있는 쪽을 클래스로 표기
    function updateContainerClass() {
    const active = buttons.find(b => b.classList.contains('active'));
    if (!active) return;
    if (active.dataset.value === 'ice') {
    segmented.classList.add('left');
    segmented.classList.remove('right');
} else {
    segmented.classList.add('right');
    segmented.classList.remove('left');
}
}
    updateContainerClass();

    // 클릭 처리
    buttons.forEach(btn => {
    btn.addEventListener('click', () => {
        buttons.forEach(b => {
            b.classList.remove('active');
            b.setAttribute('aria-selected', 'false');
        });
        btn.classList.add('active');
        btn.setAttribute('aria-selected', 'true');

        // (옵션) 현재 선택값을 콘솔에 출력하거나 다른 작업에 사용
        console.log('선택:', btn.dataset.value);
        updateContainerClass();
    });

    // 키보드 접근성: 좌/우 화살표로 이동, Enter/Space로 선택
    btn.addEventListener('keydown', (e) => {
    const idx = buttons.indexOf(btn);
    if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
    e.preventDefault();
    const prev = buttons[(idx - 1 + buttons.length) % buttons.length];
    prev.focus();
} else if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
    e.preventDefault();
    const next = buttons[(idx + 1) % buttons.length];
    next.focus();
} else if (e.key === ' ' || e.key === 'Enter') {
    e.preventDefault();
    btn.click();
}
});
});


    let appliedOptionCounts = {};
    let pricePer = 0;
    const optionPrice = 500;
    let qty = 1;
    const qtyEl = document.getElementById('qty');
    const priceEl = document.getElementById('price');

    document.addEventListener('DOMContentLoaded', function() {
        // 🔥 HTML에서 실제 가격 가져오기
        pricePer = parseInt(priceEl.dataset.actualPrice) || 2500;
        console.log('실제 메뉴 가격:', pricePer);
        updatePrice();
    });

    // 나머지 코드는 동일...
    document.getElementById('plus').onclick = () => { qty++; updatePrice(); };
    document.getElementById('minus').onclick = () => { if (qty>1) qty--; updatePrice(); };

    function updatePrice() {
        qtyEl.textContent = qty;

        // 옵션 총액 계산
        let optionTotal = 0;
        for (const key in appliedOptionCounts) {
            optionTotal += appliedOptionCounts[key] * optionPrice;
        }

        // ✅ 수정: (기본가 + 옵션총액) × 수량
        const total = (pricePer + optionTotal) * qty;
        priceEl.textContent = total.toLocaleString() + '원';
    }


    // 모달
    const detailModal = document.getElementById('detailModal');
    const optionModal = document.getElementById('optionModal');
    const applyBtn = document.getElementById('applyOptionBtn');

    applyBtn.onclick = () => {
        // 모든 옵션 row의 현재 값을 appliedOptionCounts에 저장
        document.querySelectorAll('#optionContent .option-row').forEach(row => {
            const name = row.querySelector('span').textContent;
            const val = parseInt(row.querySelector('.val').textContent);
            appliedOptionCounts[name] = val;
        });

        updatePrice();           // 총액 갱신
        optionModal.style.display = 'none'; // 모달 닫기
    };

    const detailBtn = document.getElementById('detailBtn');
    const shotBtn = document.getElementById('shotBtn');
    const sweetBtn = document.getElementById('sweetBtn');
    const toppingBtn = document.getElementById('toppingBtn');

    if (detailBtn) {
        detailBtn.onclick = () => openModal(detailModal);
    }
    if (shotBtn) {
        shotBtn.onclick = () => openOption('샷 선택', ['샷 추가']);
    }
    if (sweetBtn) {
        sweetBtn.onclick = () => openOption('당도 선택', ['바닐라 시럽 추가']);
    }
    if (toppingBtn) {
        toppingBtn.onclick = () => openOption('토핑 선택', ['휘핑 크림 추가']);
    }

    function openModal(modal) {
    modal.style.display = 'flex';
    modal.querySelector('.close').onclick = () => modal.style.display = 'none';
    modal.onclick = (e) => { if (e.target === modal) modal.style.display = 'none'; };
}

    const optionTitle = document.getElementById('optionTitle');
    const optionContent = document.getElementById('optionContent');
    function openOption(title, items) {
        optionTitle.textContent = title;
        optionContent.innerHTML = items.map(item => {
            let val = appliedOptionCounts[item] || 0;
            return `
      <div class='option-row'>
        <span>${item}</span>
        <div class='opt-controls'>
          <span>500원</span>
          <button class="option-minus-btn" data-option="${item}">-</button>
          <span class='val'>${val}</span>
          <button class="option-plus-btn" data-option="${item}">+</button>
        </div>
      </div>
    `;
        }).join('');

        // 이벤트 리스너 추가
        optionContent.querySelectorAll('.option-minus-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                adjust(this, -1);
            });
        });

        optionContent.querySelectorAll('.option-plus-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                adjust(this, 1);
            });
        });

        openModal(optionModal);
    }


    function adjust(btn, delta) {
        const valEl = btn.parentElement.querySelector('.val');
        let v = parseInt(valEl.textContent);
        v = Math.max(0, v + delta);
        valEl.textContent = v;

        // 옵션값 변경 시 실시간으로 appliedOptionCounts 업데이트
        const optionName = btn.closest('.option-row').querySelector('span').textContent;
        appliedOptionCounts[optionName] = v;
    }

    document.querySelector('.add').addEventListener('click', function() {
        addToCart();
    });

    function addToCart() {
        // 1. 현재 선택된 옵션들 수집
        const activeTempBtn = document.querySelector('.segmented-btn.active');
        let selectedTemp = 'ice'; // 푸드 메뉴를 위해 기본값 설정

        // segmented 버튼이 실제로 존재하는 경우에만 값 설정
        if (activeTempBtn && activeTempBtn.dataset.value) {
            selectedTemp = activeTempBtn.dataset.value;
        }

        let tumblerUse = document.getElementById('tumbler') ? document.getElementById('tumbler').checked : false;
        let shotCount = appliedOptionCounts['샷 추가'] || 0;
        let vanillaSyrupCount = appliedOptionCounts['바닐라 시럽 추가'] || 0;
        let whippedCreamCount = appliedOptionCounts['휘핑 크림 추가'] || 0;
        let quantity = qty;

        // 2. 메뉴 ID 가져오기 (URL 파라미터에서)
        let urlParams = new URLSearchParams(window.location.search);
        let menuId = urlParams.get('id');
        console.log('menuId:', menuId);

        if (!menuId) {
            alert('메뉴 정보를 찾을 수 없습니다.');
            return;
        }

        // 3. 데이터 객체 생성 - 푸드 메뉴도 'ice' 기본값 사용
        let cartData = {
            menuId: menuId,
            quantity: quantity,
            temp: selectedTemp, // 항상 값이 있도록
            tumblerUse: tumblerUse,
            shotCount: shotCount,
            vanillaSyrupCount: vanillaSyrupCount,
            whippedCreamCount: whippedCreamCount
        };

        console.log('담기 데이터:', cartData);

        // 4. AJAX 요청
        fetch('/home/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(cartData)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 오류');
                }
                return response.json();
            })
            .then(result => {
                if (result.success) {
                    alert('장바구니에 추가되었습니다.');
                    window.location.href = '/home/cart';
                } else {
                    alert('장바구니 추가에 실패했습니다: ' + result.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('장바구니 추가 중 오류가 발생했습니다.');
            });
    }