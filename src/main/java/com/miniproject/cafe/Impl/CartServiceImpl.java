package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.CartMapper;
import com.miniproject.cafe.Service.CartService;
import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        int totalPrice = 0;
        for (Map<String, Object> item : cartItems) {
            try {
                int menuPrice = parseIntSafe(item.get("MENU_PRICE"));
                int quantity = parseIntSafe(item.get("QUANTITY"));
                int shotCount = parseIntSafe(item.get("SHOT_COUNT"));
                int vanillaSyrupCount = parseIntSafe(item.get("VANILLA_SYRUP_COUNT"));
                int whippedCreamCount = parseIntSafe(item.get("WHIPPED_CREAM_COUNT"));

                // ✅ 수정: quantity는 한 번만 곱하기
                int basePrice = menuPrice * quantity;
                int optionPrice = (shotCount + vanillaSyrupCount + whippedCreamCount) * 500;
                int itemTotal = basePrice + (optionPrice * quantity);  // ✅ 옵션도 수량만큼

                totalPrice += itemTotal;

                // 디버깅용 출력
                System.out.println("메뉴: " + item.get("MENU_NAME") +
                        ", 기본가: " + menuPrice +
                        ", 수량: " + quantity +
                        ", 옵션가: " + optionPrice +
                        ", 아이템합계: " + itemTotal);

            } catch (Exception e) {
                System.err.println("가격 계산 중 오류: " + e.getMessage());
                continue;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cartItems", cartItems);
        result.put("totalPrice", totalPrice);
        return result;
    }

    // 🔥 안전한 정수 변환 메서드
    private int parseIntSafe(Object value) {
        if (value == null) return 0;

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            } else {
                return Integer.parseInt(value.toString());
            }
        } catch (NumberFormatException e) {
            System.err.println("숫자 변환 오류: " + value + " -> " + e.getMessage());
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
        try {
            System.out.println("장바구니 추가 시작 - 회원: " + memberId + ", 메뉴: " + menuId);

            // 1. 회원 장바구니 조회
            Long cartId = cartMapper.findCartByMemberId(memberId);
            System.out.println("기존 장바구니 ID: " + cartId);

            // 2. 장바구니가 없으면 생성
            if (cartId == null) {
                System.out.println("새 장바구니 생성");
                Map<String, Object> cartParams = new HashMap<>();
                cartParams.put("memberId", memberId);
                cartMapper.insertCartByMap(cartParams);
                cartId = cartMapper.findCartByMemberId(memberId);
                System.out.println("생성된 장바구니 ID: " + cartId);
            }

            // 3. 메뉴 옵션 조회
            Map<String, Object> optionParams = new HashMap<>();
            optionParams.put("menuId", menuId);
            optionParams.put("temp", temp);
            optionParams.put("tumblerUse", tumblerUse);
            optionParams.put("shotCount", shotCount);
            optionParams.put("vanillaSyrupCount", vanillaSyrupCount);
            optionParams.put("whippedCreamCount", whippedCreamCount);

            System.out.println("옵션 파라미터: " + optionParams);

            Long menuOptionId = cartMapper.findMenuOption(optionParams);
            System.out.println("기존 메뉴 옵션 ID: " + menuOptionId);

            // 4. 메뉴 옵션이 없으면 생성
            if (menuOptionId == null) {
                System.out.println("새 메뉴 옵션 생성");
                cartMapper.insertMenuOption(optionParams);
                menuOptionId = cartMapper.findMenuOption(optionParams);
                System.out.println("생성된 메뉴 옵션 ID: " + menuOptionId);
            }

            // 5. 카트 아이템 생성
            CartItemVO cartItemVO = new CartItemVO();
            cartItemVO.setCartId(cartId);
            cartItemVO.setMenuOptionId(menuOptionId);
            cartItemVO.setQuantity(quantity);

            System.out.println("카트 아이템: " + cartItemVO);

            int result = cartMapper.addCartItem(cartItemVO);
            System.out.println("카트 아이템 추가 결과: " + result);

            return result;

        } catch (Exception e) {
            System.err.println("장바구니 추가 중 오류: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}