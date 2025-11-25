package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.OrderItemVO;
import com.miniproject.cafe.VO.OrderVO;
import com.miniproject.cafe.VO.RecentOrderVO;

import java.util.List;

public interface OrderService {

    List<OrderVO> getOrdersByStore(String storeName); //관리자 페이지에 표시될 주문 목록
    OrderVO createOrder(OrderVO order); //주문 넣기
    void updateOrderStatus(String status, Long orderId);//주문 상태 업데이트

    List<RecentOrderVO> getRecentOrders(String memberId);
    List<RecentOrderVO> getAllOrders(String memberId);

    OrderVO getOrderById(Long orderId);

    void useCoupons(List<Integer> couponIds);

    List<OrderItemVO> getOrderItems(Long orderId);
}
