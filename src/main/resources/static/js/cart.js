// cart.js

document.addEventListener('DOMContentLoaded', function() {

    let fixedDeliveryFee = 2000;
    let currentUserId = 1; // 실제로는 로그인된 사용자 ID로 설정, 현재는 임시

    // API 기본 URL
    let API_BASE_URL = '/api/cart';

    // API 호출 함수들
    function fetchCart(userId) {
        return fetch(`${API_BASE_URL}/${userId}`)
            .then(response => response.json());
    }

    function updateCartItemQuantity(cartItemId, quantity) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}?quantity=${quantity}`, {
            method: 'PATCH'
        }).then(response => response.json());
    }

    function removeCartItem(cartItemId) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}`, {
            method: 'DELETE'
        }).then(response => response.json());
    }

    function removeSelectedItems(cartItemIds) {
        let deletePromises = cartItemIds.map(cartItemId =>
            removeCartItem(cartItemId)
        );
        return Promise.all(deletePromises);
    }

    // ------------------------------------------
    // A. 품목별 합산 가격 업데이트 함수
    // ------------------------------------------
    function updateItemPriceDisplay(itemElement) {
        let itemPricePerUnit = parseInt(itemElement.dataset.price);
        let itemQuantity = parseInt(itemElement.querySelector('.item-quantity').dataset.quantity);
        let itemTotalPrice = itemPricePerUnit * itemQuantity;

        let priceDisplayElement = itemElement.querySelector('.item-price-display');

        if (priceDisplayElement) {
            priceDisplayElement.textContent = `${itemTotalPrice.toLocaleString('ko-KR')}원`;
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

    // 장바구니 총 가격을 계산하고 UI를 업데이트하는 함수
    function updateCartTotal() {
        let total = 0;
        let items = document.querySelectorAll('.cart-item');

        items.forEach(function(item) {
            let isChecked = item.querySelector('.item-checkbox-input').checked;

            updateItemPriceDisplay(item);

            if (isChecked) {
                let itemPricePerUnit = parseInt(item.dataset.price);
                let itemQuantity = parseInt(item.querySelector('.item-quantity').dataset.quantity);
                total += itemPricePerUnit * itemQuantity;
            }
        });

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
    }

    // ------------------------------------------
    // 2. 장바구니 항목 기능 (수량/삭제/체크박스) - API 연동 추가
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
                let cartItemId = item.dataset.cartItemId; // cartItemId 가져오기

                if (btn.classList.contains('plus-btn')) {
                    newQuantity += 1;
                } else if (btn.classList.contains('minus-btn')) {
                    if (currentQuantity > 1) {
                        newQuantity -= 1;
                    }
                }

                if (newQuantity !== currentQuantity && cartItemId) {
                    // API 호출로 수량 변경
                    updateCartItemQuantity(cartItemId, newQuantity)
                        .then(updatedCart => {
                            // 성공 시 UI 업데이트
                            quantitySpan.dataset.quantity = newQuantity;
                            quantitySpan.textContent = newQuantity;
                            updateItemPriceDisplay(item);
                            updateCartTotal();
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

                    removeCartItem(cartItemId)
                        .then(() => {
                            item.remove();
                            updateCartTotal();
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
    // 5. 선택 삭제 기능 - API 연동 추가
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

            let cartItemIds = Array.from(checkedItems).map(checkbox => {
                return checkbox.closest('.cart-item').dataset.cartItemId;
            }).filter(id => id); // 유효한 ID만 필터링

            removeSelectedItems(cartItemIds)
                .then(() => {
                    // 성공 시 UI에서 제거
                    checkedItems.forEach(function(checkbox) {
                        let cartItem = checkbox.closest('.cart-item');
                        if (cartItem) {
                            cartItem.remove();
                        }
                    });

                    updateCartTotal();

                    let selectAllCheckbox = document.getElementById('selectAll');
                    if (selectAllCheckbox) {
                        selectAllCheckbox.checked = false;
                    }

                    alert('선택된 항목이 삭제되었습니다.');
                })
                .catch(error => {
                    console.error('삭제 실패:', error);
                    alert('삭제에 실패했습니다.');
                });
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

    // 페이지 로드 시 장바구니 데이터 불러오기
    loadCartData();
});