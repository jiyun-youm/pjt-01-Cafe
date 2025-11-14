package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.MenuVO;
import com.miniproject.cafe.VO.OrderDetailVO;

import java.util.List;

public interface OrderDetailService {
    List<MenuVO> getAllMenu();
    MenuVO findById(String id);
    int orderDetail(OrderDetailVO orderDetailVO);
}
