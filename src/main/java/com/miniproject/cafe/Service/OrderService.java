package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.OrderVO;

import java.util.List;

public interface OrderService {

    List<OrderVO> getOrdersByStore(String storeName); //관리자 페이지에 표시될 주문 목록
    OrderVO createOrder(OrderVO order); //주문 넣기
    void updateOrderStatus(String status, Long orderId);//주문 상태 업데이트
}
