package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.CartMapper;
import com.miniproject.cafe.Service.CartService;
import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.CartVO;
import com.miniproject.cafe.VO.MenuOptionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return cartMapper.getCartList(memberId);
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
}
