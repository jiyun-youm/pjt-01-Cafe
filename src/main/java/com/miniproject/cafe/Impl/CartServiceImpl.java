package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.CartMapper;
import com.miniproject.cafe.Service.CartService;
import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("cartService")
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Override
    public int insertCart(CartVO cartVO) {
        return cartMapper.insertCart(cartVO);
    }

    @Override
    public Map<String, Object> getCartList(String memberId) {
        List<Map<String, Object>> cartItems = cartMapper.getCartList(memberId);

        if (cartItems == null || cartItems.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("cartItems", new ArrayList<>());
            result.put("totalPrice", 0);
            return result;
        }

        int totalPrice = 0;
        for (Map<String, Object> item : cartItems) {
            if (item == null) continue;

            int menuPrice = parseIntSafe(item.get("MENU_PRICE"));
            int quantity = parseIntSafe(item.get("QUANTITY"));

            Object shotCountObj = item.get("SHOT_COUNT");
            Object vanillaSyrupCountObj = item.get("VANILLA_SYRUP_COUNT");
            Object whippedCreamCountObj = item.get("WHIPPED_CREAM_COUNT");

            int shotCount = shotCountObj != null ? parseIntSafe(shotCountObj) : 0;
            int vanillaSyrupCount = vanillaSyrupCountObj != null ? parseIntSafe(vanillaSyrupCountObj) : 0;
            int whippedCreamCount = whippedCreamCountObj != null ? parseIntSafe(whippedCreamCountObj) : 0;

            int optionCount = shotCount + vanillaSyrupCount + whippedCreamCount;

            int itemTotalPrice = (menuPrice + (optionCount * 500)) * quantity;
            totalPrice += itemTotalPrice;

            item.put("ITEM_TOTAL_PRICE", itemTotalPrice);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cartItems", cartItems);
        result.put("totalPrice", totalPrice);
        return result;
    }

    private int parseIntSafe(Object value) {
        if (value == null) {
            return 0;
        }

        try {
            String strValue = value.toString().trim();

            if (strValue.isEmpty()) {
                return 0;
            }

            if (strValue.contains(".")) {
                strValue = strValue.split("\\.")[0];
            }

            strValue = strValue.replace(",", "");
            strValue = strValue.replaceAll("[^0-9-]", "");

            if (strValue.isEmpty() || strValue.equals("-")) {
                return 0;
            }

            return Integer.parseInt(strValue);

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public int addCartItem(CartItemVO cartItemVO) {
        return cartMapper.addCartItem(cartItemVO);
    }

    @Override
    public int deleteCartItem(long cartItemId) {
        return cartMapper.deleteCartItem(cartItemId);
    }

    @Override
    public int changeQuantityCartItem(long cartItemId, int quantity) {
        return cartMapper.changeQuantityCartItem(cartItemId, quantity);
    }

    @Override
    @Transactional
    public int addToCart(String memberId, String menuId, int quantity, String temp,
                         boolean tumblerUse, int shotCount, int vanillaSyrupCount,
                         int whippedCreamCount) {

        // ✅ 방어 로직: memberId가 없으면 바로 실패 처리 (FK 오류 방지)
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("memberId is null or empty in addToCart()");
        }

        try {
            // 1. 회원 장바구니 조회
            Long cartId = cartMapper.findCartByMemberId(memberId);

            // 2. 장바구니가 없으면 생성
            if (cartId == null) {
                Map<String, Object> cartParams = new HashMap<>();
                cartParams.put("memberId", memberId);
                cartMapper.insertCartByMap(cartParams);
                cartId = cartMapper.findCartByMemberId(memberId);
            }

            // 3. 메뉴 옵션 조회
            Map<String, Object> optionParams = new HashMap<>();
            optionParams.put("menuId", menuId);
            optionParams.put("temp", temp);
            optionParams.put("tumblerUse", tumblerUse);
            optionParams.put("shotCount", shotCount);
            optionParams.put("vanillaSyrupCount", vanillaSyrupCount);
            optionParams.put("whippedCreamCount", whippedCreamCount);

            Long menuOptionId = cartMapper.findMenuOption(optionParams);

            // 4. 메뉴 옵션이 없으면 생성
            if (menuOptionId == null) {
                cartMapper.insertMenuOption(optionParams);
                menuOptionId = cartMapper.findMenuOption(optionParams);
            }

            // 5. 기존에 같은 메뉴 옵션이 장바구니에 있는지 확인
            Long existingCartItemId = cartMapper.findExistingCartItem(cartId, menuOptionId);

            if (existingCartItemId != null) {
                CartItemVO existingItem = cartMapper.getCartItem(existingCartItemId);
                int newQuantity = existingItem.getQuantity() + quantity;
                return cartMapper.changeQuantityCartItem(existingCartItemId, newQuantity);
            } else {
                CartItemVO cartItemVO = new CartItemVO();
                cartItemVO.setCartId(cartId);
                cartItemVO.setMenuOptionId(menuOptionId);
                cartItemVO.setQuantity(quantity);
                return cartMapper.addCartItem(cartItemVO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void clearCart(String memberId) {
        cartMapper.deleteAll(memberId);
    }
}