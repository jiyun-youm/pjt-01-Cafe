package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.CartMapper;
import com.miniproject.cafe.Service.CartService;
import com.miniproject.cafe.VO.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("cartService")
public class CartImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Override
    public int insertCart(CartVO cartVO) {
        return cartMapper.insertCart(cartVO);
    }
}
