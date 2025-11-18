package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CartMapper {

    // 기존 메서드들
    int insertCart(CartVO cartVO);
    List<Map<String, Object>> getCartList(String memberId);
    int addCartItem(CartItemVO cartItemVO);
    int deleteCartItem(long cartItemId);
    int changeQuantityCartItem(@Param("cartItemId") long cartItemId, @Param("quantity") int quantity);

    Long findCartByMemberId(String memberId);
    int insertCartByMap(Map<String, Object> cartParams); // 이름 변경
    Long findMenuOption(Map<String, Object> optionParams);
    int insertMenuOption(Map<String, Object> optionParams);
    Long findExistingCartItem(@Param("cartId") Long cartId, @Param("menuOptionId") Long menuOptionId);
    CartItemVO getCartItem(@Param("cartItemId") Long cartItemId);
}