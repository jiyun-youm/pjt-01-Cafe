package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.CartVO;

import java.util.Map;

public interface CartService {

    int insertCart(CartVO cartVO); // 장바구니 생성
    Map<String, Object> getCartList(String memberId); // 장바구니 목록 조회
    int addCartItem(CartItemVO cartItemVO); // 장바구니에 담기
    int deleteCartItem(long cartItemId); // 아이템 개별 삭제
    int changeQuantityCartItem(long cartItemId, int quantity); // 메뉴 수량 변경
    int addToCart(String memberId,
                  String menuId,
                  int quantity,
                  String temp,
                  boolean tumblerUse,
                  int shotCount,
                  int vanillaSyrupCount,
                  int whippedCreamCount
    );
    void clearCart(String memberId);
}
