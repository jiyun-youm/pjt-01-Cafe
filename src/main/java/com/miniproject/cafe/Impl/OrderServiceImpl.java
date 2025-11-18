package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.OrderMapper;
import com.miniproject.cafe.Service.OrderService;
import com.miniproject.cafe.VO.OrderItemVO;
import com.miniproject.cafe.VO.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // (추가)

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<OrderVO> getOrdersByStore(String storeName) {
        return orderMapper.findOrdersByStore(storeName);
    }

    //신규 주문
    @Override
    @Transactional // 트랜잭션 필수: 주문서 생성과 상세 메뉴 저장이 모두 성공해야 커밋됨
    public OrderVO createOrder(OrderVO order) {

        // 1. 기본값 세팅
        if (order.getOrderStatus() == null) {
            order.setOrderStatus("주문접수");
        }
        if (order.getOrderType() == null) {
            order.setOrderType("매장");
        }
        if (order.getStoreName() == null) {
            order.setStoreName("");
        }

        // 2. orders 테이블에 저장 (여기서 DB가 생성한 orderId가 order 객체에 담김)
        order.setOrderTime(new java.util.Date());
        orderMapper.insertOrder(order);

        // 3. 상세 메뉴(Item)들이 있다면, 방금 생성된 주문번호(orderId)를 넣고 저장
        List<OrderItemVO> items = order.getOrderItemList();

        if (items != null && !items.isEmpty()) {
            for (OrderItemVO item : items) {
                // 방금 insertOrder를 통해 생성된 주문 번호를 세팅
                item.setOrderId(order.getOrderId());
            }
            // Mapper에 추가했던 다중 insert 메서드 호출
            orderMapper.insertOrderDetails(items);
        }

        return order;
    }

    //주문 업데이트 (완료, 취소)
    @Override
    @Transactional
    public void updateOrderStatus(String status, Long orderId) {
        orderMapper.updateOrderStatus(status, orderId);
    }
}