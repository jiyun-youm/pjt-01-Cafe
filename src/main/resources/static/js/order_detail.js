document.addEventListener('DOMContentLoaded', () => {
    // 초기화 실행
    initPrice();
    initSegmentedControl();
    initModals();
    initActionButtons();
});

// ==============================
// 1. 전역 변수 및 가격 계산 로직
// ==============================
let appliedOptionCounts = {}; // 옵션 저장용 객체 (예: {'샷 추가': 1})
let qty = 1;                  // 기본 수량
const optionPrice = 500;      // 옵션 단가
let pricePer = 0;             // 메뉴 1개당 기본 가격

const qtyEl = document.getElementById('qty');
const priceEl = document.getElementById('price');

// 초기 가격 설정 함수
function initPrice() {
    if (priceEl) {
        // HTML의 data-actual-price 속성에서 가격 가져오기 (없으면 0원 처리)
        pricePer = parseInt(priceEl.dataset.actualPrice) || 0;
        updatePrice();
    }

    // 수량 조절 버튼 이벤트
    const plusBtn = document.getElementById('plus');
    const minusBtn = document.getElementById('minus');

    if (plusBtn) plusBtn.onclick = () => { qty++; updatePrice(); };
    if (minusBtn) minusBtn.onclick = () => { if (qty > 1) qty--; updatePrice(); };
}

// 화면에 가격/수량 업데이트
function updatePrice() {
    if (!qtyEl || !priceEl) return;

    qtyEl.textContent = qty;

    // 옵션 총액 계산
    let optionTotal = 0;
    for (const key in appliedOptionCounts) {
        optionTotal += appliedOptionCounts[key] * optionPrice;
    }

    // 최종 가격 = (기본가 + 옵션총액) * 수량
    const total = (pricePer + optionTotal) * qty;
    priceEl.textContent = total.toLocaleString() + '원';
}

// ==============================
// 2. ICE / HOT 선택 (Segmented Control)
// ==============================
function initSegmentedControl() {
    const segmented = document.querySelector('.segmented');
    const buttons = Array.from(document.querySelectorAll('.segmented-btn'));

    if (!segmented || buttons.length === 0) return; // 버튼 없으면 실행 안함

    function updateContainerClass() {
        const active = buttons.find(b => b.classList.contains('active'));
        if (!active) return;

        // active 버튼의 값에 따라 배경 위치 조정 (CSS 클래스 토글)
        segmented.classList.toggle('left', active.dataset.value === 'ice');
        segmented.classList.toggle('right', active.dataset.value === 'hot');
    }

    // 초기 실행
    updateContainerClass();

    // 클릭 이벤트 등록
    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            buttons.forEach(b => b.classList.remove('active')); // 기존 활성 제거
            btn.classList.add('active'); // 클릭한 것 활성화
            updateContainerClass();
        });
    });
}

// ==============================
// 3. 모달(Modal) & 옵션 관리 로직
// ==============================
const detailModal = document.getElementById('detailModal');
const optionModal = document.getElementById('optionModal');
const optionTitle = document.getElementById('optionTitle');
const optionContent = document.getElementById('optionContent');

function initModals() {
    // 버튼 연결
    bindModalOpen('detailBtn', detailModal);
    bindOptionOpen('shotBtn', '샷 선택', ['샷 추가']);
    bindOptionOpen('sweetBtn', '당도 선택', ['바닐라 시럽 추가']);
    bindOptionOpen('toppingBtn', '토핑 선택', ['휘핑 크림 추가']);

    // 옵션 적용 버튼 (모달 닫기)
    const applyBtn = document.getElementById('applyOptionBtn');
    if (applyBtn) {
        applyBtn.onclick = () => {
            updatePrice(); // 가격 갱신
            optionModal.style.display = 'none';
        };
    }
}

// 단순 모달 열기 연결
function bindModalOpen(btnId, modalEl) {
    const btn = document.getElementById(btnId);
    if (btn && modalEl) {
        btn.onclick = () => openModal(modalEl);
    }
}

// 옵션 모달 열기 연결
function bindOptionOpen(btnId, title, items) {
    const btn = document.getElementById(btnId);
    if (btn) {
        btn.onclick = () => openOptionModal(title, items);
    }
}

// 모달 공통 열기 함수
function openModal(modal) {
    if (!modal) return;
    modal.style.display = 'flex';

    // 닫기 버튼(X) 이벤트
    const closeBtn = modal.querySelector('.close');
    if (closeBtn) closeBtn.onclick = () => modal.style.display = 'none';

    // 배경 클릭 시 닫기
    modal.onclick = (e) => {
        if (e.target === modal) modal.style.display = 'none';
    };
}

// 옵션 모달 내용 생성 및 열기
function openOptionModal(title, items) {
    if (!optionTitle || !optionContent) return;

    optionTitle.textContent = title;

    // HTML 생성 (현재 선택된 수량 반영)
    optionContent.innerHTML = items.map(item => {
        let val = appliedOptionCounts[item] || 0;
        return `
        <div class='option-row'>
            <span>${item}</span>
            <div class='opt-controls'>
                <span>500원</span>
                <button class="option-minus-btn" type="button" data-option="${item}">-</button>
                <span class='val'>${val}</span>
                <button class="option-plus-btn" type="button" data-option="${item}">+</button>
            </div>
        </div>
        `;
    }).join("");

    // + / - 버튼 이벤트 연결
    optionContent.querySelectorAll('.option-minus-btn').forEach(btn => {
        btn.onclick = function() { adjustOption(this, -1); };
    });
    optionContent.querySelectorAll('.option-plus-btn').forEach(btn => {
        btn.onclick = function() { adjustOption(this, 1); };
    });

    openModal(optionModal);
}

// 옵션 수량 변경 함수
function adjustOption(btn, delta) {
    const valEl = btn.parentElement.querySelector('.val');
    let v = parseInt(valEl.textContent);
    v = Math.max(0, v + delta); // 0보다 작아지지 않게
    valEl.textContent = v;

    // 전역 변수에 저장
    const optionName = btn.dataset.option;
    appliedOptionCounts[optionName] = v;
}


// ==============================
// 4. 데이터 수집 및 전송 (핵심)
// ==============================

// 현재 화면의 상태(메뉴, 옵션, 수량 등)를 객체로 리턴
function collectCurrentState() {
    // 1. 메뉴 ID 가져오기
    let menuId = document.getElementById('menuId') ? document.getElementById('menuId').value : null;

    // URL 파라미터 백업 로직
    if (!menuId) {
        const urlParams = new URLSearchParams(window.location.search);
        menuId = urlParams.get('id');
    }

    // 2. 메뉴 이름
    const menuName = document.getElementById('menuName') ? document.getElementById('menuName').value : "메뉴";

    // 3. 기타 옵션들
    const activeBtn = document.querySelector('.segmented-btn.active');
    const temp = activeBtn ? activeBtn.dataset.value : 'ice';
    const tumblerEl = document.getElementById('tumbler');
    const tumblerUse = tumblerEl ? tumblerEl.checked : false;
    const shotCount = appliedOptionCounts['샷 추가'] || 0;
    const vanillaSyrupCount = appliedOptionCounts['바닐라 시럽 추가'] || 0;
    const whippedCreamCount = appliedOptionCounts['휘핑 크림 추가'] || 0;

    return {
        // 🔥 [중요 수정] Number()를 제거했습니다. "GN001" 같은 문자열도 허용됩니다.
        menuId: menuId,
        menuName: menuName,
        quantity: qty,
        temp: temp,
        tumblerUse: tumblerUse,
        shotCount: shotCount,
        vanillaSyrupCount: vanillaSyrupCount,
        whippedCreamCount: whippedCreamCount
    };
}

function initActionButtons() {
    // [장바구니 담기] 버튼
    const cartBtn = document.querySelector('.add');
    if (cartBtn) {
        cartBtn.addEventListener('click', addToCart);
    }

    // [주문하기] 버튼
    const orderBtn = document.querySelector('.order');
    if (orderBtn) {
        orderBtn.addEventListener('click', async (e) => {
            e.preventDefault();

            // 1. HTML에서 필수 정보 추출
            const storeName = document.getElementById('detailStoreName')?.value || "";
            const uId = document.getElementById('detailMemberId')?.value || "guest";
            const menuId = document.getElementById('menuId')?.value;

            // 2. UI에서 값 추출
            const currentQty = parseInt(document.getElementById('qty').textContent || 1);
            const total = parseInt(priceEl?.textContent.replace(/[^0-9]/g, '') || 0);
            const tempValue = document.querySelector('.segmented-btn.active')?.dataset.value || 'ICE';

            // 3. 옵션 데이터 정확히 추출
            const tumblerEl = document.getElementById('tumbler'); // ✅ 수정: tumblerCheck → tumbler
            const tumblerUse = tumblerEl ? tumblerEl.checked : false;

            const shotCount = appliedOptionCounts['샷 추가'] || 0;
            const vanillaSyrupCount = appliedOptionCounts['바닐라 시럽 추가'] || 0;
            const whippedCreamCount = appliedOptionCounts['휘핑 크림 추가'] || 0;

            console.log("🔍 옵션 데이터 확인:", {
                shotCount,
                vanillaSyrupCount,
                whippedCreamCount,
                tumblerUse,
                tempValue
            });

            // 4. 필수 유효성 검사
            if (!storeName) {
                alert("매장 정보가 없습니다.");
                return;
            }
            if (!menuId) {
                alert("메뉴 정보를 찾을 수 없습니다.");
                return;
            }

            // 5. 옵션 데이터 매핑
            const itemData = {
                menuId: menuId,
                menuItemName: document.getElementById('menuName')?.value || "Unknown",
                quantity: currentQty,
                temp: tempValue,
                tumbler: tumblerUse ? 1 : 0,
                shot: shotCount,
                vanillaSyrup: vanillaSyrupCount,
                whippedCream: whippedCreamCount
            };

            // 6. 최종 페이로드 구성
            const orderPayload = {
                totalQuantity: currentQty,
                totalPrice: total,
                orderType: "매장",
                orderStatus: "주문접수",
                uId: uId,
                storeName: storeName,
                orderItemList: [itemData]
            };

            // 7. API 전송
            try {
                const response = await fetch("/api/orders/create", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(orderPayload)
                });

                if (response.ok) {
                    alert("주문이 성공적으로 접수되었습니다!");
                    window.location.href = "/home/";
                } else {
                    const errorText = await response.text();
                    throw new Error("서버 처리 실패: " + errorText);
                }
            } catch (e) {
                console.error("❌ 주문 실패:", e.message);
                alert("주문 처리 중 오류가 발생했습니다.");
            }
        });
    }
}

// 장바구니 담기 함수 (AJAX)
function addToCart() {
    const data = collectCurrentState();

    if (!data.menuId) {
        alert("메뉴 정보를 찾을 수 없습니다. (menuId Missing)");
        return;
    }

    // 장바구니용 데이터 구조 매핑
    const cartPayload = {
        menuId: data.menuId,
        quantity: data.quantity,
        temp: data.temp,
        tumblerUse: data.tumblerUse,
        shotCount: data.shotCount,
        vanillaSyrupCount: data.vanillaSyrupCount,
        whippedCreamCount: data.whippedCreamCount
    };

    fetch('/home/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(cartPayload)
    })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                if (confirm('장바구니에 담겼습니다.\n장바구니로 이동하시겠습니까?')) {
                    window.location.href = '/home/cart';
                }
            } else {
                alert('장바구니 담기 실패: ' + result.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert('오류가 발생했습니다.');
        });
}

// 주문하기 함수 (AJAX)
async function placeOrder() {
    const data = collectCurrentState();

    if (!data.menuId) {
        alert("메뉴 정보를 찾을 수 없습니다. (menuId Missing)");
        return;
    }

    // 현재 화면에 표시된 최종 가격 숫자만 추출
    const priceText = document.getElementById('price').textContent;
    const finalTotalPrice = parseInt(priceText.replace(/[^0-9]/g, ''));

    // ⭐ OrderVO 구조에 맞춘 데이터 생성
    const orderPayload = {
        totalQuantity: data.quantity,
        totalPrice: finalTotalPrice,
        orderType: "매장",
        orderStatus: "주문접수",
        uId: "guest",

        // 상세 메뉴 리스트
        orderItemList: [
            {
                // 🚨 [수정됨] Number(data.menuId)를 제거하고 data.menuId 그대로 사용
                menuId: data.menuId,
                menuItemName: data.menuName,
                quantity: data.quantity
            }
        ]
    };

    try {
        const response = await fetch("/api/orders/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(orderPayload)
        });

        if (response.ok) {
            alert("주문이 성공적으로 접수되었습니다!");
            window.location.href = "/home/";
        } else {
            // 에러 메시지 확인을 위해 로그 출력
            const errorText = await response.text();
            console.log("서버 에러 내용:", errorText);
            alert("주문 처리에 실패했습니다.");
        }

    } catch (e) {
        console.error(e);
        alert("서버 통신 오류가 발생했습니다.");
    }
}