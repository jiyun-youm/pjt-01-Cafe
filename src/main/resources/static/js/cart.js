document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll("input[name='couponIds']").forEach(cb => {
        cb.removeAttribute('disabled');
    });
});


document.addEventListener('DOMContentLoaded', function () {

    /* ---------------------------------------
       1. 기본 설정
    --------------------------------------- */
    let mainElement = document.querySelector('.shopping-cart-combined');
    let currentUserId = mainElement ? mainElement.getAttribute('data-member-id') : null;

    let fixedDeliveryFee = 2000;
    let isProcessingPayment = false;
    let API_BASE_URL = '/home/cart';


    /* ---------------------------------------
       2. 메뉴 보러가기 버튼
    --------------------------------------- */
    const goToMenuBtn = document.querySelector('.go-to-menu-btn');

    if (goToMenuBtn) {
        goToMenuBtn.addEventListener('click', async function (e) {
            e.preventDefault();

            try {
                const resp = await fetch("/home/getRegion");
                const storeName = await resp.text();

                if (!storeName || storeName === "null" || storeName.trim() === "") {
                    alert("주문할 매장을 먼저 선택해주세요.");
                    window.location.href = '/home/';
                } else {
                    window.location.href = '/menu/coffee';
                }
            } catch (error) {
                alert("매장 정보를 확인하는 중 오류가 발생했습니다.");
                window.location.href = '/home/';
            }
        });
    }


    /* ---------------------------------------
       3. Cart API
    --------------------------------------- */
    function changeQuantityCartItem(cartItemId, quantity) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}?quantity=${quantity}`, {
            method: 'PATCH'
        }).then(r => r.text());
    }

    function deleteCartItem(cartItemId) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}`, {
            method: 'DELETE'
        }).then(r => r.text());
    }


    /* ---------------------------------------
       4. 개별 아이템 가격 표시
    --------------------------------------- */
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


    /* ---------------------------------------
       5. 쿠폰 할인 계산
    --------------------------------------- */
    function calculateCouponDiscount() {
        let selectedCoupons = document.querySelectorAll("input[name='couponIds']:checked").length;
        if (selectedCoupons === 0) return 0;

        let drinkItems = [];

        document.querySelectorAll('.cart-item').forEach(item => {
            let isChecked = item.querySelector('.item-checkbox-input').checked;
            if (!isChecked) return;

            if (item.dataset.category === '푸드') return;

            let price = parseInt(item.dataset.basePrice);
            let qty = parseInt(item.querySelector('.item-quantity').dataset.quantity);

            for (let i = 0; i < qty; i++) {
                drinkItems.push(price);
            }
        });

        if (drinkItems.length === 0) return 0;

        drinkItems.sort((a, b) => b - a);

        let discount = 0;
        for (let i = 0; i < selectedCoupons && i < drinkItems.length; i++) {
            discount += drinkItems[i];
        }

        return discount;
    }


    /* ---------------------------------------
       6. 요청사항 직접입력
    --------------------------------------- */
    let directInputCheck = document.getElementById('directInputCheck');
    let directRequest = document.getElementById('directRequest');

    if (directRequest) {
        directRequest.disabled = true;
        directRequest.style.opacity = "0.5";
    }

    if (directInputCheck && directRequest) {
        directInputCheck.addEventListener('change', function () {
            if (this.checked) {
                directRequest.disabled = false;
                directRequest.style.opacity = "1";
            } else {
                directRequest.disabled = true;
                directRequest.value = "";
                directRequest.style.opacity = "0.5";
            }
        });
    }


    /* ---------------------------------------
       7. 가격 업데이트
    --------------------------------------- */
    function updateOrderPrice(productTotal) {
        let discount = calculateCouponDiscount();

        document.getElementById('productPrice').textContent =
            `${productTotal.toLocaleString('ko-KR')}원`;

        let deliveryFee = 0;
        let deliveryBtn = document.querySelector('.delivery-btn.active-delivery');
        if (deliveryBtn && deliveryBtn.dataset.type === 'delivery') {
            deliveryFee = fixedDeliveryFee;
        }

        document.getElementById('deliveryFee').textContent =
            deliveryFee === 0 ? '0원' : `${deliveryFee.toLocaleString('ko-KR')}원`;

        let final = productTotal - discount + deliveryFee;
        if (final < 0) final = 0;

        document.getElementById('finalTotalPrice').textContent =
            `${final.toLocaleString('ko-KR')}원`;
    }


    /* ---------------------------------------
       8. 장바구니 합계
    --------------------------------------- */
    function updateCartTotal() {
        let total = 0;

        document.querySelectorAll('.cart-item').forEach(item => {
            let checked = item.querySelector('.item-checkbox-input').checked;
            if (!checked) return;

            let basePrice = parseInt(item.dataset.basePrice) || 0;
            let optionPrice = parseInt(item.dataset.optionPrice) || 0;
            let qty = parseInt(item.querySelector('.item-quantity').dataset.quantity) || 0;

            total += (basePrice + optionPrice) * qty;
        });

        document.getElementById('totalCartPrice').textContent =
            `${total.toLocaleString('ko-KR')}원`;

        updateOrderPrice(total);
        updatePaymentButtonState();
    }


    /* ---------------------------------------
       9. 쿠폰 UI 토글
    --------------------------------------- */
    const toggleBtn = document.getElementById("couponToggleBtn");
    const couponList = document.getElementById("couponList");

    if (toggleBtn) {
        toggleBtn.addEventListener("click", () => {
            couponList.style.display =
                couponList.style.display === "block" ? "none" : "block";
        });
    }


    /* ---------------------------------------
       10. 쿠폰 적용 결과 표시
    --------------------------------------- */
    const couponApplyResult = document.getElementById("couponApplyResult");
    const couponSummaryText = document.getElementById("couponSummaryText");
    const couponAppliedItems = document.getElementById("couponAppliedItems");
    const couponDiscountTotal = document.getElementById("couponDiscountTotal");

    document.addEventListener("change", function (e) {
        if (e.target.name !== "couponIds") return;

        const checked = document.querySelectorAll("input[name='couponIds']:checked");

        if (checked.length === 0) {
            couponApplyResult.style.display = "none";
            updateCartTotal();
            return;
        }

        couponApplyResult.style.display = "block";

        let drinkList = [];
        document.querySelectorAll(".cart-item").forEach(item => {
            if (item.dataset.category === "푸드") return;

            let qty = parseInt(item.querySelector('.item-quantity').dataset.quantity);
            let name = item.querySelector('.item-name').textContent.trim();
            let price = parseInt(item.dataset.basePrice);

            for (let i = 0; i < qty; i++) {
                drinkList.push({ name, price });
            }
        });

        drinkList.sort((a, b) => b.price - a.price);

        let html = "";
        let discount = 0;

        checked.forEach((c, idx) => {
            if (!drinkList[idx]) return;

            html += `<li>${drinkList[idx].name} - 할인 적용: ${drinkList[idx].price.toLocaleString("ko-KR")}원</li>`;
            discount += drinkList[idx].price;
        });

        couponSummaryText.textContent = `적용 쿠폰: ${checked.length}개`;
        couponAppliedItems.innerHTML = html;
        couponDiscountTotal.textContent =
            `총 할인 금액: ${discount.toLocaleString("ko-KR")}원`;

        updateCartTotal();
        limitCouponSelection();
    });


    /* ---------------------------------------
       11. 쿠폰 개수 제한
    --------------------------------------- */
    function limitCouponSelection() {
        const couponCheckboxes = document.querySelectorAll("input[name='couponIds']");
        const checked = document.querySelectorAll("input[name='couponIds']:checked");

        let drinkCount = 0;

        document.querySelectorAll('.cart-item').forEach(item => {
            if (item.dataset.category !== "푸드") {
                let qty = parseInt(item.querySelector('.item-quantity').dataset.quantity) || 0;
                drinkCount += qty;
            }
        });

        if (checked.length > drinkCount) {
            alert("음료 수량보다 많은 쿠폰을 선택할 수 없습니다.");
            checked[checked.length - 1].checked = false;
            const evt = new Event("change", { bubbles: true });
            checked[0].dispatchEvent(evt);

            return;
        }
    }


    /* ---------------------------------------
       12. 장바구니 항목 이벤트
    --------------------------------------- */
    let cartContainer = document.querySelector('.item-list');
    if (cartContainer) {
        cartContainer.addEventListener('click', function (e) {
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

            else if (btn.classList.contains('item-checkbox-input')) {
                updateCartTotal();
            }
        });
    }


    /* ---------------------------------------
       13. 전체 선택 체크박스
    --------------------------------------- */
    let selectAllCheckbox = document.getElementById('selectAll');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            let itemCheckboxes = document.querySelectorAll('.item-checkbox-input');
            itemCheckboxes.forEach(cb => cb.checked = selectAllCheckbox.checked);
            updateCartTotal();
        });
    }


    /* ---------------------------------------
       14. 배달/포장/매장 토글 버튼
    --------------------------------------- */
    let deliveryToggle = document.querySelector('.delivery-toggle');
    if (deliveryToggle) {
        deliveryToggle.addEventListener('click', function (e) {
            let btn = e.target.closest('.delivery-btn');
            if (!btn) return;

            deliveryToggle.querySelectorAll('.delivery-btn')
                .forEach(b => b.classList.remove('active-delivery'));

            btn.classList.add('active-delivery');

            updateCartTotal();
        });
    }


    /* ---------------------------------------
       15. 선택 삭제 버튼
    --------------------------------------- */
    let deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function () {
            let checkedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');
            if (checkedItems.length === 0) {
                alert('삭제할 항목을 선택해주세요.');
                return;
            }

            if (!confirm(`선택된 ${checkedItems.length}개 항목을 삭제하시겠습니까?`)) return;

            let deletePromises = [];
            checkedItems.forEach(function (checkbox) {
                let cartItem = checkbox.closest('.cart-item');
                let cartItemId = cartItem.dataset.cartItemId;
                if (cartItemId) deletePromises.push(deleteCartItem(cartItemId));
            });

            Promise.all(deletePromises).then(() => window.location.reload());
        });
    }


    /* ---------------------------------------
       16. 요청사항 문자열 생성
    --------------------------------------- */
    function getRequestText() {
        let arr = [];

        document.querySelectorAll('input[name="requestOption"]:checked')
            .forEach(el => arr.push(el.value));

        if (directRequest && directRequest.value.trim() !== "") {
            arr.push(directRequest.value.trim());
        }

        return arr.length === 0 ? "없음" : arr.join(", ");
    }


    /* ---------------------------------------
       17. 주문 데이터 생성
    --------------------------------------- */
    function preparePaymentData(selectedItems) {
        let storeName = document.getElementById('currentStoreName')?.value ?? "";

        let orderItemList = [];
        let totalQty = 0;

        selectedItems.forEach(cb => {
            let item = cb.closest('.cart-item');
            let qty = parseInt(item.querySelector('.item-quantity').dataset.quantity);

            // 🔥 [핵심 수정] HTML dataset에서 옵션 값을 꺼내와야 합니다.
            // (HTML에 data-shot, data-vanilla-syrup, data-whipped-cream 속성이 있어야 함)
            let shotVal = parseInt(item.dataset.shot) || 0;
            let syrupVal = parseInt(item.dataset.vanillaSyrup) || 0;
            let creamVal = parseInt(item.dataset.whippedCream) || 0;

            orderItemList.push({
                menuId: item.dataset.menuId,
                menuItemName: item.querySelector('.item-name').textContent.trim(),
                quantity: qty,
                optionId: parseInt(item.dataset.optionId),
                temp: item.querySelector('.item-temp')?.textContent.trim() ?? 'ICE',
                tumbler: item.querySelector('.item-options')?.textContent.includes('텀블러') ? 1 : 0,

                // 🔥 [추가] 자바 VO 이름과 똑같이 맞춰서 넣어줍니다.
                shot: shotVal,
                vanillaSyrup: syrupVal,
                whippedCream: creamVal
            });

            totalQty += qty;
        });

        // ... (아래 로직은 기존과 동일) ...
        let deliveryBtn = document.querySelector('.delivery-btn.active-delivery');
        let orderType = "포장";

        if (deliveryBtn) {
            if (deliveryBtn.dataset.type === 'delivery') orderType = "배달";
            else if (deliveryBtn.dataset.type === 'store') orderType = "매장";
            else if (deliveryBtn.dataset.type === 'packaging') orderType = "포장";
        }

        let finalStr = document.getElementById('finalTotalPrice').textContent;
        let finalPrice = parseInt(finalStr.replace(/[^0-9]/g, ''));

        let selectedCoupons =
            Array.from(document.querySelectorAll("input[name='couponIds']:checked"))
                .map(c => parseInt(c.value));

        return {
            totalQuantity: totalQty,
            totalPrice: finalPrice,
            orderType: orderType,
            orderStatus: "주문접수",
            uId: currentUserId,
            storeName: storeName,
            orderItemList: orderItemList,
            requestText: getRequestText(),
            couponIds: selectedCoupons
        };
    }


    /* ---------------------------------------
       18. 결제 요청
    --------------------------------------- */
    async function handlePayment() {

        if (isProcessingPayment) return;

        let selectedItems =
            document.querySelectorAll('.cart-item .item-checkbox-input:checked');

        if (selectedItems.length === 0) {
            alert("결제할 상품을 선택해주세요.");
            return;
        }

        isProcessingPayment = true;
        setPaymentButtonLoading(true);

        let data = preparePaymentData(selectedItems);

        try {
            let response = await fetch("/api/orders/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            let deletePromises = [];
            selectedItems.forEach(cb => {
                let cartItem = cb.closest('.cart-item');
                deletePromises.push(deleteCartItem(cartItem.dataset.cartItemId));
            });

            await Promise.all(deletePromises);

            alert("주문이 완료되었습니다!");
            window.location.href = "/home/";

        } catch (err) {
            alert("주문 처리 오류: " + err.message);
        } finally {
            isProcessingPayment = false;
            setPaymentButtonLoading(false);
        }
    }


    /* ---------------------------------------
       19. 버튼 상태
    --------------------------------------- */
    function setPaymentButtonLoading(isLoading) {
        let btn = document.querySelector('.payment-btn');
        if (!btn) return;

        btn.disabled = isLoading;
        btn.textContent = isLoading ? "주문 처리 중..." : "결제하기";
    }

    function updatePaymentButtonState() {
        let btn = document.querySelector('.payment-btn');
        let count =
            document.querySelectorAll('.cart-item .item-checkbox-input:checked').length;

        if (!btn) return;

        btn.disabled = count === 0;
        btn.style.opacity = count === 0 ? "0.6" : "1";
    }


    /* ---------------------------------------
       20. 이벤트 등록 & 초기 계산
    --------------------------------------- */
    document.querySelector('.payment-btn')
        ?.addEventListener('click', handlePayment);

    updateCartTotal();
});
