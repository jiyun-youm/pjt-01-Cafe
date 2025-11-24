document.addEventListener('DOMContentLoaded', function() {

    // 1. 초기 설정
    let mainElement = document.querySelector('.shopping-cart-combined');
    let currentUserId = mainElement ? mainElement.getAttribute('data-member-id') : null;

    let fixedDeliveryFee = 2000;
    let isProcessingPayment = false;
    let API_BASE_URL = '/home/cart';

    // ==========================================
    // 2. "메뉴 보러가기" 버튼 지점 선택 체크
    // ==========================================

    let goToMenuBtn = document.querySelector('.go-to-menu-btn');

    if (goToMenuBtn) {
        goToMenuBtn.addEventListener('click', async function(e) {
            e.preventDefault();

            try {
                let resp = await fetch("/home/getRegion");
                let storeName = await resp.text();

                if (!storeName || storeName === "null" || storeName.trim() === "") {
                    alert("주문할 매장을 먼저 선택해주세요.");
                    window.location.href = '/home/';
                } else {
                    // 지점 선택되었으면 정상적으로 메뉴 페이지로 이동
                    window.location.href = '/menu/coffee';
                }
            } catch (error) {
                console.error("매장 확인 중 오류:", error);
                alert("매장 정보를 확인하는 중 오류가 발생했습니다.");
                window.location.href = '/home/';
            }
        });
    }

    // ==========================================
    // 2. 장바구니 API 통신 함수 (수량변경, 삭제)
    // ==========================================

    // 수량 변경 API 호출
    function changeQuantityCartItem(cartItemId, quantity) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}?quantity=${quantity}`, {
            method: 'PATCH'
        }).then(response => response.text());
    }

    // 삭제 API 호출
    function deleteCartItem(cartItemId) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}`, {
            method: 'DELETE'
        }).then(response => response.text());
    }

    // ==========================================
    // 3. 화면 업데이트 & 가격 계산 로직
    // ==========================================

    // 개별 아이템 가격 표시 업데이트
    function updateItemPriceDisplay(itemElement) {
        let basePrice = parseInt(itemElement.dataset.basePrice) || 0;
        let optionPrice = parseInt(itemElement.dataset.optionPrice) || 0;
        let quantity = parseInt(itemElement.querySelector('.item-quantity').dataset.quantity) || 0;

        let itemTotalPrice = (basePrice + optionPrice) * quantity;

        let priceDisplayElement = itemElement.querySelector('.item-price-display');
        if (priceDisplayElement) {
            priceDisplayElement.textContent = `${itemTotalPrice.toLocaleString('ko-KR')}원`;
        }
    }

    // ==========================================
    // 직접입력 체크박스 활성/비활성 제어
    // ==========================================

    let directInputCheck = document.getElementById('directInputCheck');
    let directRequest = document.getElementById('directRequest');

    // 초기 상태: 비활성화
    if (directRequest) {
        directRequest.disabled = true;
        directRequest.style.opacity = "0.5";  // 시각적으로 비활성화 느낌
    }

    if (directInputCheck && directRequest) {
        directInputCheck.addEventListener('change', function() {
            if (this.checked) {
                // 활성화
                directRequest.disabled = false;
                directRequest.style.opacity = "1";
            } else {
                // 비활성화 + 내용 삭제
                directRequest.disabled = true;
                directRequest.value = "";
                directRequest.style.opacity = "0.5";
            }
        });
    }

    // 하단 주문 정보(총액, 배달비) 업데이트
    function updateOrderPrice(productTotal) {
        // 상품 금액
        let productPriceElement = document.getElementById('productPrice');
        if (productPriceElement) productPriceElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;

        let summaryTotalElement = document.getElementById('summaryTotalPrice');
        if (summaryTotalElement) summaryTotalElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;

        // 배달비 계산
        let currentDeliveryFee = 0;
        let deliveryButton = document.querySelector('.delivery-btn.active-delivery');
        if (deliveryButton && deliveryButton.dataset.type === 'delivery') {
            currentDeliveryFee = fixedDeliveryFee;
        }
        // 포장과 매장은 배달비 0원

        let deliveryFeeElement = document.getElementById('deliveryFee');
        if (deliveryFeeElement) {
            deliveryFeeElement.textContent = currentDeliveryFee > 0 ? `${currentDeliveryFee.toLocaleString('ko-KR')}원` : '0원';
        }

        // 최종 결제 금액
        let finalTotal = productTotal + currentDeliveryFee;
        let finalTotalElement = document.getElementById('finalTotalPrice');
        if (finalTotalElement) finalTotalElement.textContent = `${finalTotal.toLocaleString('ko-KR')}원`;
    }

    // 전체 장바구니 합계 재계산
    function updateCartTotal() {
        let total = 0;
        let items = document.querySelectorAll('.cart-item');

        items.forEach(function(item) {
            // 체크된 항목만 합계에 포함
            let isChecked = item.querySelector('.item-checkbox-input').checked;
            if (isChecked) {
                let basePrice = parseInt(item.dataset.basePrice) || 0;
                let optionPrice = parseInt(item.dataset.optionPrice) || 0;
                let quantity = parseInt(item.querySelector('.item-quantity').dataset.quantity) || 0;
                total += (basePrice + optionPrice) * quantity;
            }
        });

        let formattedTotal = total.toLocaleString('ko-KR');
        let cartTotalElement = document.getElementById('totalCartPrice');
        if (cartTotalElement) cartTotalElement.textContent = `${formattedTotal}원`;

        updateOrderPrice(total);
        updatePaymentButtonState(); // 버튼 활성화 여부 체크
    }

    // ==========================================
    // 4. 이벤트 리스너 (클릭, 수량조절, 삭제)
    // ==========================================
    let cartContainer = document.querySelector('.item-list');
    if (cartContainer) {
        cartContainer.addEventListener('click', function(e) {
            let btn = e.target;
            let item = btn.closest('.cart-item');
            if (!item) return;

            // 수량 조절 (+ / -)
            if (btn.classList.contains('plus-btn') || btn.classList.contains('minus-btn')) {
                let quantitySpan = item.querySelector('.item-quantity');
                let currentQuantity = parseInt(quantitySpan.dataset.quantity);
                let newQuantity = currentQuantity;
                let cartItemId = item.dataset.cartItemId;

                if (btn.classList.contains('plus-btn')) {
                    newQuantity += 1;
                } else if (btn.classList.contains('minus-btn')) {
                    if (currentQuantity > 1) newQuantity -= 1;
                }

                if (newQuantity !== currentQuantity && cartItemId) {
                    changeQuantityCartItem(cartItemId, newQuantity).then(result => {
                        if (result === "change success") {
                            quantitySpan.dataset.quantity = newQuantity;
                            quantitySpan.textContent = newQuantity;
                            updateItemPriceDisplay(item);
                            updateCartTotal();
                        } else {
                            alert('수량 변경에 실패했습니다.');
                        }
                    });
                }
            }
            // 삭제 버튼 (x)
            else if (btn.classList.contains('item-remove')) {
                let cartItemId = item.dataset.cartItemId;
                if (cartItemId) {
                    if (!confirm('정말 삭제하시겠습니까?')) return;
                    deleteCartItem(cartItemId).then(result => {
                        if (result === "delete success") {
                            window.location.reload();
                        } else {
                            alert('삭제에 실패했습니다.');
                        }
                    });
                }
            }
            // 개별 체크박스
            else if (btn.classList.contains('item-checkbox-input')) {
                updateCartTotal();
            }
        });
    }

    // 전체 선택 체크박스
    let selectAllCheckbox = document.getElementById('selectAll');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            let itemCheckboxes = document.querySelectorAll('.item-checkbox-input');
            itemCheckboxes.forEach(cb => cb.checked = selectAllCheckbox.checked);
            updateCartTotal();
        });
    }

    // 배달/포장/매장 토글 버튼
    let deliveryToggle = document.querySelector('.delivery-toggle');
    if (deliveryToggle) {
        deliveryToggle.addEventListener('click', function(e) {
            if (e.target.classList.contains('delivery-btn')) {
                deliveryToggle.querySelectorAll('.delivery-btn').forEach(btn => btn.classList.remove('active-delivery'));
                e.target.classList.add('active-delivery');
                updateCartTotal();
            }
        });
    }

    // 선택 삭제 버튼
    let deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            let checkedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');
            if (checkedItems.length === 0) {
                alert('삭제할 항목을 선택해주세요.');
                return;
            }

            if (!confirm(`선택된 ${checkedItems.length}개 항목을 삭제하시겠습니까?`)) return;

            let deletePromises = [];
            checkedItems.forEach(function(checkbox) {
                let cartItem = checkbox.closest('.cart-item');
                let cartItemId = cartItem.dataset.cartItemId;
                if (cartItemId) deletePromises.push(deleteCartItem(cartItemId));
            });

            Promise.all(deletePromises).then(() => window.location.reload());
        });
    }

    //요청사항
    function getRequestText() {
        let items = [];

        // 체크박스(예: "빨대 빼주세요", "얼음 적게") 값 읽기
        document.querySelectorAll('input[name="requestOption"]:checked')
            .forEach(el => items.push(el.value));

        // 직접입력 textarea 값 읽기
        let directInput = document.getElementById('directRequest');
        if (directInput && directInput.value.trim() !== "") {
            items.push(directInput.value.trim());
        }

        // 아무 옵션도 선택 안 했으면 '없음'
        return items.length > 0 ? items.join(", ") : "없음";
    }

    // ==========================================
    // 5. ⭐ [핵심] 주문 데이터 생성 (OrderVO 구조 맞춤) - 수정된 부분
    // ==========================================
    function preparePaymentData(selectedItems) {
        let storeNameInput = document.getElementById('currentStoreName');
        let storeName = storeNameInput ? storeNameInput.value : "";

        let orderItems = [];
        let totalQty = 0;

        let requestText = getRequestText();

        console.log("--------------------------------");
        console.log("🛒 [JS 데이터 점검]");
        console.log("1. 매장명(HTML hidden):", storeNameInput);
        console.log("2. 매장명(Value):", storeName);
        console.log("3. 주문자 ID:", currentUserId);
        console.log("--------------------------------");

        // 1. 체크된 장바구니 아이템들을 하나씩 순회하며 데이터 추출
        selectedItems.forEach(function(checkbox) {
            let item = checkbox.closest('.cart-item');
            let qty = parseInt(item.querySelector('.item-quantity').dataset.quantity);

            //옵션 ID
            let optionId = parseInt(item.dataset.optionId);
            console.log('[CHECK] cart item optionId =', optionId);

            // 메뉴 ID
            let menuId = item.dataset.menuId || item.dataset.cartItemId;

            // 상세 옵션 정보
            let shot = parseInt(item.dataset.shotCount || 0);
            let vanillaSyrup = parseInt(item.dataset.vanillaSyrupCount || 0);
            let whippedCream = parseInt(item.dataset.whippedCreamCount || 0);

            // 온도 (ICE/HOT)
            let tempElement = item.querySelector('.item-temp');
            let tempText = tempElement ? tempElement.textContent.trim() : 'ICE';

            // 텀블러 사용 여부
            let optionsText = item.querySelector('.item-options') ? item.querySelector('.item-options').textContent : "";
            let isTumbler = optionsText.includes('텀블러') ? 1 : 0;

            // 리스트에 추가
            orderItems.push({
                menuId: menuId,
                menuItemName: item.querySelector('.item-name').textContent.trim(),
                quantity: qty,
                optionId: optionId,
                temp: tempText,
                tumbler: isTumbler,
                shot: shot,
                vanillaSyrup: vanillaSyrup,
                whippedCream: whippedCream
            });

            totalQty += qty;
        });

        // 2. 주문 유형 (배달/포장/매장) 확인
        let deliveryBtn = document.querySelector('.delivery-btn.active-delivery');
        let pickupMethod = "포장"; // 기본값

        if (deliveryBtn) {
            if (deliveryBtn.dataset.type === 'delivery') {
                pickupMethod = "배달";
            } else if (deliveryBtn.dataset.type === 'packaging') {
                pickupMethod = "포장";
            } else if (deliveryBtn.dataset.type === 'store') {
                pickupMethod = "매장";
            }
        }

        // 3. 총 결제 금액 (화면에 계산된 최종 금액에서 숫자만 추출)
        let totalStr = document.getElementById('finalTotalPrice').textContent;
        let finalPrice = parseInt(totalStr.replace(/[^0-9]/g, ''));

        // 5. 최종 데이터 반환 (OrderVO 구조)
        return {
            totalQuantity: totalQty,
            totalPrice: finalPrice,
            orderType: pickupMethod, // "배달", "포장", "매장" 중 하나
            orderStatus: "주문접수",
            uId: currentUserId || "guest",
            storeName: storeName,
            orderItemList: orderItems,
            requestText: requestText
        };
    }

    // ==========================================
    // 7. ⭐ [핵심] 결제 요청 (API 호출)
    // ==========================================
    async function handlePayment() {
        if (isProcessingPayment) return;

        let selectedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');
        if (selectedItems.length === 0) {
            alert('결제할 상품을 선택해주세요.');
            return;
        }

        isProcessingPayment = true;
        setPaymentButtonLoading(true);

        let paymentData = preparePaymentData(selectedItems);

        try {
            // 1. 주문 생성 API 호출
            let response = await fetch('/api/orders/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(paymentData)
            });

            if (!response.ok) {
                let errorText = await response.text();
                throw new Error('주문 실패: ' + errorText);
            }

            // 2. ✅ 주문 성공 시 DB에서 선택된 장바구니 아이템들 삭제
            let deletePromises = [];
            selectedItems.forEach(function(checkbox) {
                let cartItem = checkbox.closest('.cart-item');
                let cartItemId = cartItem.dataset.cartItemId;
                if (cartItemId) {
                    // 🔥 이 함수가 DB 테이블에서 row를 삭제하는 API 호출
                    deletePromises.push(deleteCartItem(cartItemId));
                }
            });

            // 모든 삭제 API 호출이 완료될 때까지 대기
            await Promise.all(deletePromises);

            console.log('✅ 주문 생성 및 장바구니 삭제 완료');
            alert('주문이 완료되었습니다!');
            window.location.href = "/home/";

        } catch (error) {
            console.error('주문 처리 중 오류:', error);
            alert('주문 처리 중 오류가 발생했습니다: ' + error.message);
        } finally {
            setPaymentButtonLoading(false);
            isProcessingPayment = false;
        }
    }

    // 결제 버튼 UI 상태 관리
    function setPaymentButtonLoading(isLoading) {
        let btn = document.querySelector('.payment-btn');
        if (btn) {
            if (isLoading) {
                btn.disabled = true;
                btn.textContent = '주문 처리 중...';
            } else {
                btn.disabled = false;
                btn.textContent = '결제하기';
            }
        }
    }

    function updatePaymentButtonState() {
        let btn = document.querySelector('.payment-btn');
        let count = document.querySelectorAll('.cart-item .item-checkbox-input:checked').length;
        if (btn) {
            btn.disabled = count === 0;
            btn.style.opacity = count === 0 ? '0.6' : '1';
        }
    }

    // 결제 버튼 이벤트 연결
    let paymentButton = document.querySelector('.payment-btn');
    if (paymentButton) {
        paymentButton.addEventListener('click', handlePayment);
    }

    // 페이지 로드 시 초기 계산 실행
    updateCartTotal();
});