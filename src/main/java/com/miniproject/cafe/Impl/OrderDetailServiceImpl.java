package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.OrderDetailMapper;
import com.miniproject.cafe.Service.OrderDetailService;
import com.miniproject.cafe.VO.MenuVO;
import com.miniproject.cafe.VO.OrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;

@Service("OrderDetailService")
public class OrderDetailServiceImpl implements OrderDetailService {


    @Autowired
    private OrderDetailMapper orderDetailMapper;



//    @Override
//    public List<MenuVO> getAllMenu() {
//        return List.of();
//    }

    @Override
    public MenuVO findById(String menuId) {
       // int menuId = Integer.parseInt(id); // id가 String이면 int로 변환
        return orderDetailMapper.findById(menuId);
    }

    @Override
    public int orderDetail(OrderDetailVO orderDetailVO) {
        return orderDetailMapper.orderDetail(orderDetailVO);
    }
}
