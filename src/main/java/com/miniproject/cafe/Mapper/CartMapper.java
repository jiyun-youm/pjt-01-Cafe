package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.CartVO;
import com.miniproject.cafe.VO.MenuOptionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface CartMapper {

    int insertCart(CartVO cartVO);
    Map<String, Object> getCartList(String memberId); // 장바구니 목록 조회
    int addCartItem(CartItemVO cartItemVO);
    int deleteCartItem(long cartItemId);
    int changeQuantityCartItem(long cartItemId, int quantity);

}
