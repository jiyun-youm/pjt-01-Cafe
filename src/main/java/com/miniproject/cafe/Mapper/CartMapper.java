package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.CartVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper {

    int insertCart(CartVO cartVO);

}
