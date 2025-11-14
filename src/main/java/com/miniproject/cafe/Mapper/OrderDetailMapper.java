package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MenuVO;
import com.miniproject.cafe.VO.OrderDetailVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    List<MenuVO> getAllMenu();
    MenuVO getMenuById(int menuId);
    int orderDetail(OrderDetailVO orderDetailVO);
}
