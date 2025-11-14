
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
    const pricePer = 2500;
    const optionPrice = 500;
    let qty = 1;
    const qtyEl = document.getElementById('qty');
    const priceEl = document.getElementById('price');
    document.getElementById('plus').onclick = () => { qty++; updatePrice(); };
    document.getElementById('minus').onclick = () => { if (qty>1) qty--; updatePrice(); };
    function updatePrice() {
    qtyEl.textContent = qty;

    let optionTotal = 0;
    for (const key in appliedOptionCounts) {
    optionTotal += appliedOptionCounts[key] * optionPrice;
}

    const total = (qty * pricePer) + optionTotal;
    priceEl.textContent = total.toLocaleString() + '원';
}


    // 모달
    const detailModal = document.getElementById('detailModal');
    const optionModal = document.getElementById('optionModal');
    const applyBtn = document.getElementById('applyOptionBtn');

    applyBtn.onclick = () => {
    // 모든 옵션 row를 돌면서 수량을 저장
    document.querySelectorAll('#optionContent .option-row').forEach(row => {
        const name = row.querySelector('span').textContent;
        const val = parseInt(row.querySelector('.val').textContent);
        appliedOptionCounts[name] = val;
    });

    updatePrice();           // 총액 갱신
    optionModal.style.display = 'none'; // 모달 닫기
};

    document.getElementById('detailBtn').onclick = () => openModal(detailModal);
    document.getElementById('shotBtn').onclick = () => openOption('샷 선택', ['샷 추가']);
    document.getElementById('sweetBtn').onclick = () => openOption('당도 선택', ['바닐라 시럽 추가']);
    document.getElementById('toppingBtn').onclick = () => openOption('토핑 선택', ['휘핑 크림 추가']);

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
    // 기존 선택값 불러오기
    let val = appliedOptionCounts[item] || 0;
    return `
      <div class='option-row'>
        <span>${item}</span>
        <div class='opt-controls'>
          <span>500원</span>
          <button onclick='adjust(this,-1)'>-</button>
          <span class='val'>${val}</span>
          <button onclick='adjust(this,1)'>+</button>
        </div>
      </div>
    `;
}).join('');

    openModal(optionModal);
}


    function adjust(btn, delta) {
    const valEl = btn.parentElement.querySelector('.val');
    let v = parseInt(valEl.textContent);
    v = Math.max(0, v + delta);
    valEl.textContent = v;

}