// cart.js

document.addEventListener('DOMContentLoaded', function() {

    let mainElement = document.querySelector('.shopping-cart-combined');
    let currentUserId = mainElement ? mainElement.getAttribute('data-member-id') : null;

    if (!currentUserId) {
        console.error('사용자 ID 없음 - 로그인 확인 필요');
        alert('로그인이 필요합니다.');
        window.location.href = '/';
        return;
    }

    let fixedDeliveryFee = 2000;
    let isProcessingPayment = false;
    let API_BASE_URL = '/home/cart';

    // 실제 존재하는 API만 사용
    function fetchCart(userId) {
        return fetch(`${API_BASE_URL}/list/${userId}`)
            .then(response => response.json());
    }

    function changeQuantityCartItem(cartItemId, quantity) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}?quantity=${quantity}`, {
            method: 'PATCH'
        }).then(response => response.text());
    }

    function deleteCartItem(cartItemId) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}`, {
            method: 'DELETE'
        }).then(response => response.text());
    }

    // ------------------------------------------
    // A. 품목별 가격 계산 (단순화)
    // ------------------------------------------
    function updateItemPriceDisplay(itemElement) {
        let basePrice = parseInt(itemElement.dataset.basePrice) || 0;
        let optionPrice = parseInt(itemElement.dataset.optionPrice) || 0;
        let quantity = parseInt(itemElement.querySelector('.item-quantity').dataset.quantity) || 0;

        // ✅ 단순 계산: (기본가 + 옵션가) * 수량
        let itemTotalPrice = (basePrice + optionPrice) * quantity;

        console.log('아이템 가격 계산:', basePrice, '+', optionPrice, '*', quantity, '=', itemTotalPrice);

        let priceDisplayElement = itemElement.querySelector('.item-price-display');
        if (priceDisplayElement) {
            priceDisplayElement.textContent = `${itemTotalPrice.toLocaleString('ko-KR')}원`;
        }
    }

    function checkEmptyCart() {
        const items = document.querySelectorAll('.cart-item');
        const emptyMsg = document.querySelector('.empty-cart-message');
        const actionBar = document.querySelector('.action-bar');
        const itemList = document.querySelector('.item-list');

        if (items.length === 0) {
            document.querySelector('.action-bar').style.display = 'none';
            document.querySelector('.item-list').style.display = 'none';
            emptyMsg.style.display = 'block';
        } else {
            emptyMsg.style.display = 'none';
            if (actionBar) actionBar.style.display = 'block';
            if (itemList) itemList.style.display = 'block';
        }
    }

    // ------------------------------------------
    // 1. 가격 업데이트 및 배달비 계산 함수
    // ------------------------------------------
    function updateOrderPrice(productTotal) {
        let orderDetails = document.querySelector('.order-details');
        if (!orderDetails) return;

        let productPriceElement = document.getElementById('productPrice');
        if (productPriceElement) {
            productPriceElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;
        }

        let summaryTotalElement = document.getElementById('summaryTotalPrice');
        if (summaryTotalElement) {
            summaryTotalElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;
        }

        let currentDeliveryFee = 0;
        let deliveryFeeElement = document.getElementById('deliveryFee');

        let deliveryButton = document.querySelector('.delivery-btn[data-type="delivery"]');

        if (deliveryButton && deliveryButton.classList.contains('active-delivery')) {
            currentDeliveryFee = fixedDeliveryFee;
        }

        if (deliveryFeeElement) {
            deliveryFeeElement.textContent = currentDeliveryFee > 0 ? `${currentDeliveryFee.toLocaleString('ko-KR')}원` : '0원';
        }

        let finalTotal = productTotal + currentDeliveryFee;
        let finalTotalElement = document.getElementById('finalTotalPrice');

        if (finalTotalElement) {
            finalTotalElement.textContent = `${finalTotal.toLocaleString('ko-KR')}원`;
        }
    }

    // 안전한 정수 변환 함수
    function safeParseInt(value, defaultValue = 0) {
        if (value === null || value === undefined) return defaultValue;
        let num = parseInt(value);
        return isNaN(num) ? defaultValue : num;
    }

    // 장바구니 총 가격을 계산하고 UI를 업데이트하는 함수
    function updateCartTotal() {
        let total = 0;
        let items = document.querySelectorAll('.cart-item');

        items.forEach(function(item) {
            let isChecked = item.querySelector('.item-checkbox-input').checked;

            if (isChecked) {
                let basePrice = parseInt(item.dataset.basePrice) || 0;
                let optionPrice = parseInt(item.dataset.optionPrice) || 0;
                let quantity = parseInt(item.querySelector('.item-quantity').dataset.quantity) || 0;

                let itemTotal = (basePrice + optionPrice) * quantity;
                console.log('총합에 추가:', itemTotal);
                total += itemTotal;
            }
        });

        console.log('🎯 최종 총합:', total);

        let formattedTotal = total.toLocaleString('ko-KR');
        let cartTotalElement = document.getElementById('totalCartPrice');
        if (cartTotalElement) {
            cartTotalElement.textContent = `${formattedTotal}원`;
        }

        updateOrderPrice(total);

        let selectAllCheckbox = document.getElementById('selectAll');
        let remainingItems = document.querySelectorAll('.cart-item').length;
        if (selectAllCheckbox && remainingItems === 0) {
            selectAllCheckbox.checked = false;
        }

        // 결제 버튼 상태 업데이트
        updatePaymentButtonState();
    }

    // ------------------------------------------
    // 2. 장바구니 항목 기능 (수량/삭제/체크박스) - UI만 동작
    // ------------------------------------------
    let cartContainer = document.querySelector('.item-list');

    if (cartContainer) {
        cartContainer.addEventListener('click', function(e) {
            let btn = e.target;
            let item = btn.closest('.cart-item');
            if (!item) return;

            if (btn.classList.contains('plus-btn') || btn.classList.contains('minus-btn')) {
                let quantitySpan = item.querySelector('.item-quantity');
                let currentQuantity = parseInt(quantitySpan.dataset.quantity);
                let newQuantity = currentQuantity;
                let cartItemId = item.dataset.cartItemId;

                if (btn.classList.contains('plus-btn')) {
                    newQuantity += 1;
                } else if (btn.classList.contains('minus-btn')) {
                    if (currentQuantity > 1) {
                        newQuantity -= 1;
                    }
                }

                if (newQuantity !== currentQuantity && cartItemId) {
                    // 🔥 API 호출로 DB 업데이트
                    changeQuantityCartItem(cartItemId, newQuantity)
                        .then(result => {
                            if (result === "change success") {
                                quantitySpan.dataset.quantity = newQuantity;
                                quantitySpan.textContent = newQuantity;
                                updateItemPriceDisplay(item);
                                updateCartTotal();
                            } else {
                                alert('수량 변경에 실패했습니다.');
                            }
                        })
                        .catch(error => {
                            console.error('수량 변경 실패:', error);
                            alert('수량 변경에 실패했습니다.');
                        });
                }
            } else if (btn.classList.contains('item-remove')) {
                let cartItemId = item.dataset.cartItemId;
                if (cartItemId) {
                    if (!confirm('정말 삭제하시겠습니까?')) return;

                    deleteCartItem(cartItemId)
                        .then((result) => {
                            if (result === "delete success") {
                                // ✅ 페이지 새로고침
                                window.location.reload();
                            } else {
                                alert('삭제에 실패했습니다.');
                            }
                        })
                        .catch(error => {
                            console.error('삭제 실패:', error);
                            alert('삭제에 실패했습니다.');
                        });
                }
            } else if (btn.classList.contains('item-checkbox-input')) {
                let selectAllCheckbox = document.getElementById('selectAll');
                let allChecked = Array.from(document.querySelectorAll('.item-checkbox-input')).every(cb => cb.checked);
                if (selectAllCheckbox) {
                    selectAllCheckbox.checked = allChecked;
                }
                updateCartTotal();
            }
        });

        // 전체 선택/해제 기능
        let selectAllCheckbox = document.getElementById('selectAll');
        if(selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', function() {
                let itemCheckboxes = document.querySelectorAll('.item-checkbox-input');
                itemCheckboxes.forEach(function(checkbox) {
                    checkbox.checked = selectAllCheckbox.checked;
                });
                updateCartTotal();
            });
        }

        // 초기 로드 시 총 가격 계산
        updateCartTotal();
    }

    // ------------------------------------------
    // 3. 주문 상세 기능 (배달/포장 토글)
    // ------------------------------------------
    let deliveryToggle = document.querySelector('.delivery-toggle');

    if (deliveryToggle) {
        deliveryToggle.addEventListener('click', function(e) {
            if (e.target.classList.contains('delivery-btn')) {
                deliveryToggle.querySelectorAll('.delivery-btn').forEach(function(btn) {
                    btn.classList.remove('active-delivery');
                });

                e.target.classList.add('active-delivery');
                updateCartTotal();
            }
        });
    }

    // ------------------------------------------
    // 4. 요청사항 직접입력 활성화/비활성화 기능
    // ------------------------------------------
    let directInputCheckbox = document.getElementById('directInputCheck');
    let requestInputTextarea = document.getElementById('requestInput');

    if (directInputCheckbox && requestInputTextarea) {
        directInputCheckbox.addEventListener('change', function() {
            let isChecked = directInputCheckbox.checked;
            requestInputTextarea.disabled = !isChecked;

            if (!isChecked) {
                requestInputTextarea.value = '';
            }
        });

        requestInputTextarea.disabled = !directInputCheckbox.checked;
    }

    // ------------------------------------------
    // 5. 선택 삭제 기능 - DB 연동
    // ------------------------------------------
    let deleteSelectedBtn = document.getElementById('deleteSelectedBtn');

    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            let checkedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');

            if (checkedItems.length === 0) {
                alert('삭제할 항목을 선택해주세요.');
                return;
            }

            if (!confirm(`선택된 ${checkedItems.length}개의 항목을 장바구니에서 삭제하시겠습니까?`)) {
                return;
            }

            // 🔥 각 항목별로 개별 삭제 API 호출
            let deletePromises = [];
            let itemsToRemove = [];

            checkedItems.forEach(function(checkbox) {
                let cartItem = checkbox.closest('.cart-item');
                let cartItemId = cartItem.dataset.cartItemId;

                if (cartItemId) {
                    itemsToRemove.push(cartItem);
                    deletePromises.push(deleteCartItem(cartItemId));
                }
            });

            // 🔥 모든 삭제 요청이 완료되면 UI 업데이트
            Promise.all(deletePromises)
                .then(results => {
                    let successCount = results.filter(result => result === "delete success").length;

                    if (successCount === itemsToRemove.length) {
                        // ✅ 모든 삭제 성공
                        itemsToRemove.forEach(item => item.remove());
                        updateCartTotal();

                        let selectAllCheckbox = document.getElementById('selectAll');
                        if (selectAllCheckbox) {
                            selectAllCheckbox.checked = false;
                        }

                        alert('선택된 항목이 삭제되었습니다.');
                        window.location.reload();
                    } else {
                        alert('일부 항목 삭제에 실패했습니다.');
                    }
                })
                .catch(error => {
                    console.error('선택 삭제 실패:', error);
                    alert('삭제에 실패했습니다.');
                });
        });
    }

    // ✅ 장바구니가 비었는지 확인하고 empty 메시지를 표시하는 함수
    function checkAndShowEmptyCartMessage() {
        let cartItems = document.querySelectorAll('.cart-item');
        let cartContainer = document.querySelector('.item-list');
        let actionBar = document.querySelector('.action-bar');
        let emptyMessage = document.querySelector('.empty-cart-message');

        if (cartItems.length === 0) {
            // 장바구니 항목이 없으면 action-bar와 item-list 숨기기
            if (actionBar) actionBar.style.display = 'none';
            if (cartContainer) cartContainer.style.display = 'none';

            // empty 메시지 표시
            if (emptyMessage) {
                emptyMessage.style.display = 'block';
            }
        } else {
            // 장바구니 항목이 있으면 action-bar와 item-list 표시
            if (actionBar) actionBar.style.display = 'block';
            if (cartContainer) cartContainer.style.display = 'block';

            // empty 메시지 숨기기
            if (emptyMessage) {
                emptyMessage.style.display = 'none';
            }
        }
    }

    // ✅ 개별 삭제 시에도 호출되도록 수정
    function deleteCartItem(cartItemId) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}`, {
            method: 'DELETE'
        }).then(response => response.text())
            .then(result => {
                if (result === "delete success") {
                    // 삭제 성공 시 empty 메시지 확인
                    setTimeout(checkAndShowEmptyCartMessage, 100);
                }
                return result;
            });
    }

    // ------------------------------------------
    // 6. 초기 장바구니 데이터 로드
    // ------------------------------------------
    function loadCartData() {
        fetchCart(currentUserId)
            .then(cartData => {
                // 여기서 서버에서 받은 데이터로 UI 업데이트
                console.log('장바구니 데이터:', cartData);
                // 실제 구현시에는 cartData로 DOM을 업데이트하는 로직 추가
            })
            .catch(error => {
                console.error('장바구니 로드 실패:', error);
            });
    }

    // ------------------------------------------
    // 7. 모바일 앱 결제하기 기능
    // ------------------------------------------

    function initializePaymentButton() {
        let paymentButton = document.querySelector('.payment-btn');

        if (paymentButton) {
            // 모바일 터치 이벤트 최적화
            paymentButton.addEventListener('click', handlePayment);
            paymentButton.addEventListener('touchstart', function(e) {
                e.preventDefault(); // 모바일에서 터치 시 기본 동작 방지
            }, { passive: false });

            // 장바구니 상태 실시간 감지
            observeCartChanges();

            // 초기 상태 업데이트
            updatePaymentButtonState();
        }
    }

    // 결제 처리 함수 - 모바일 최적화
    async function handlePayment() {
        if (isProcessingPayment) return;

        let selectedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');

        // 유효성 검사
        if (!validatePayment(selectedItems)) {
            return;
        }

        // 결제 데이터 준비
        let paymentData = preparePaymentData(selectedItems);

        // 결제 처리
        await processMobilePayment(paymentData);
    }

    // 결제 유효성 검사
    function validatePayment(selectedItems) {
        if (selectedItems.length === 0) {
            showMobileAlert('결제할 상품을 선택해주세요.');
            return false;
        }

        let deliveryType = document.querySelector('.delivery-btn.active-delivery');
        if (!deliveryType) {
            showMobileAlert('배달 또는 포장을 선택해주세요.');
            return false;
        }

        let paymentMethod = document.querySelector('input[name="paymentMethod"]:checked');
        if (!paymentMethod) {
            showMobileAlert('결제수단을 선택해주세요.');
            return false;
        }

        return true;
    }

    // 모바일 알림 표시
    function showMobileAlert(message) {
        // 모바일 네이티브 알림 스타일
        let alertDiv = document.createElement('div');
        alertDiv.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: rgba(0,0,0,0.8);
            color: white;
            padding: 15px 20px;
            border-radius: 10px;
            z-index: 10000;
            font-size: 14px;
            text-align: center;
            max-width: 80%;
        `;
        alertDiv.textContent = message;
        document.body.appendChild(alertDiv);

        setTimeout(() => {
            document.body.removeChild(alertDiv);
        }, 2000);
    }

    // 선택된 상품 데이터 수집
    function getSelectedItemsData() {
        let selectedItems = [];
        let checkedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');

        checkedItems.forEach(function(checkbox) {
            let item = checkbox.closest('.cart-item');
            selectedItems.push({
                cartItemId: item.dataset.cartItemId,
                name: item.querySelector('.item-name').textContent,
                temperature: item.querySelector('.item-temp').textContent,
                price: parseInt(item.dataset.price),
                quantity: parseInt(item.querySelector('.item-quantity').dataset.quantity),
                options: item.querySelector('.item-options') ? item.querySelector('.item-options').textContent : ''
            });
        });

        return selectedItems;
    }

    // 요청사항 데이터 수집
    function getRequestData() {
        let requests = [];
        let checkedRequests = document.querySelectorAll('.request-option-checkbox:checked');

        checkedRequests.forEach(function(checkbox) {
            if (checkbox.id !== 'directInputCheck') {
                let label = checkbox.closest('label').textContent.trim();
                requests.push(label);
            }
        });

        // 직접입력 내용 추가
        let directInput = document.getElementById('requestInput');
        if (directInput && directInput.value.trim() !== '') {
            requests.push(directInput.value.trim());
        }

        return requests;
    }

    // 최종 결제 금액 계산
    function calculateFinalTotal() {
        let productTotal = 0;
        let checkedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');

        checkedItems.forEach(function(checkbox) {
            let item = checkbox.closest('.cart-item');
            let itemPrice = parseInt(item.dataset.price);
            let quantity = parseInt(item.querySelector('.item-quantity').dataset.quantity);
            productTotal += itemPrice * quantity;
        });

        // 배달비 추가
        let deliveryFee = 0;
        let deliveryButton = document.querySelector('.delivery-btn[data-type="delivery"]');
        if (deliveryButton && deliveryButton.classList.contains('active-delivery')) {
            deliveryFee = fixedDeliveryFee;
        }

        return productTotal + deliveryFee;
    }

    // 결제 데이터 준비
    function preparePaymentData(selectedItems) {
        return {
            items: getSelectedItemsData(),
            deliveryType: document.querySelector('.delivery-btn.active-delivery').dataset.type,
            paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value,
            requests: getRequestData(),
            totalAmount: calculateFinalTotal(),
            timestamp: Date.now(),
            device: 'mobile'
        };
    }

    // 모바일 결제 처리
    async function processMobilePayment(paymentData) {
        isProcessingPayment = true;
        let paymentButton = document.querySelector('.payment-btn');

        try {
            // UI 상태 변경
            setPaymentButtonLoading(true);

            // 모바일 앱 브릿지 호출 (가정)
            if (window.MobileAppBridge) {
                // 네이티브 앱 결제 호출
                let result = await window.MobileAppBridge.processPayment(JSON.stringify(paymentData));
                handlePaymentResult(result);
            } else {
                // 웹뷰 환경에서의 결제 처리
                let result = await processWebViewPayment(paymentData);
                handlePaymentResult(result);
            }
        } catch (error) {
            console.error('결제 처리 오류:', error);
            showMobileAlert('결제 처리 중 오류가 발생했습니다.');
            setPaymentButtonLoading(false);
            isProcessingPayment = false;
        }
    }

    // 웹뷰 환경 결제 처리
    async function processWebViewPayment(paymentData) {
        // 모바일 앱 내 API 호출
        const response = await fetch('/api/mobile/payment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Device-Type': 'mobile'
            },
            body: JSON.stringify(paymentData)
        });

        if (!response.ok) {
            throw new Error('결제 요청 실패');
        }

        return await response.json();
    }

    // 결제 결과 처리
    function handlePaymentResult(result) {
        setPaymentButtonLoading(false);
        isProcessingPayment = false;

        if (result.success) {
            // 결제 성공
            showPaymentSuccess(result);
        } else {
            // 결제 실패
            showMobileAlert(result.message || '결제에 실패했습니다.');
        }
    }

    // 결제 성공 처리
    function showPaymentSuccess(result) {
        // 성공 애니메이션 또는 리다이렉트
        let paymentButton = document.querySelector('.payment-btn');
        paymentButton.textContent = '결제 완료!';
        paymentButton.style.backgroundColor = '#4CAF50';

        setTimeout(() => {
            // 주문 완료 페이지로 이동 또는 앱 내 네비게이션
            if (window.MobileAppBridge) {
                window.MobileAppBridge.navigateToOrderComplete(result.orderId);
            } else {
                window.location.href = `/order/complete?orderId=${result.orderId}`;
            }
        }, 1000);
    }

    // 결제 버튼 로딩 상태 설정
    function setPaymentButtonLoading(isLoading) {
        let paymentButton = document.querySelector('.payment-btn');

        if (isLoading) {
            paymentButton.classList.add('loading');
            paymentButton.disabled = true;
            paymentButton.textContent = '결제 중...';
        } else {
            paymentButton.classList.remove('loading');
            paymentButton.disabled = false;
            paymentButton.textContent = '결제하기';
        }
    }

    // 장바구니 변화 감지 (MutationObserver)
    function observeCartChanges() {
        const cartContainer = document.querySelector('.item-list');

        if (cartContainer) {
            const observer = new MutationObserver(() => {
                updatePaymentButtonState();
            });

            observer.observe(cartContainer, {
                childList: true,
                subtree: true,
                attributes: true,
                attributeFilter: ['class', 'data-quantity']
            });
        }
    }

    // 결제 버튼 상태 업데이트
    function updatePaymentButtonState() {
        let paymentButton = document.querySelector('.payment-btn');
        let selectedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');

        if (selectedItems.length === 0) {
            paymentButton.disabled = true;
            paymentButton.style.opacity = '0.6';
        } else {
            paymentButton.disabled = false;
            paymentButton.style.opacity = '1';
        }
    }

    // ------------------------------------------
    // 초기화 실행
    // ------------------------------------------

    // 페이지 로드 시 장바구니 데이터 불러오기
    loadCartData();

    // 모바일 결제 버튼 초기화
    initializePaymentButton();

    // 모바일 백버튼 처리 (선택사항)
    document.addEventListener('backbutton', function(event) {
        if (isProcessingPayment) {
            // 결제 중에는 백버튼 무시
            event.preventDefault();
            showMobileAlert('결제 진행 중입니다.');
        }
    }, false);

});