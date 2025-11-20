package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Emitter.SseEmitterStore;
import com.miniproject.cafe.Mapper.OrderDetailMapper;
import com.miniproject.cafe.Mapper.OrderMapper;
import com.miniproject.cafe.Service.OrderService;
import com.miniproject.cafe.Service.RewardService;
import com.miniproject.cafe.VO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SseEmitterStore emitterStore;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RewardService rewardService;


    @Override
    public List<OrderVO> getOrdersByStore(String storeName) {
        return orderMapper.findOrdersByStore(storeName);
    }

    // 신규 주문 생성
    @Override
    @Transactional
    public OrderVO createOrder(OrderVO order) {

        // 기본값 설정
        if (order.getOrderStatus() == null) order.setOrderStatus("주문접수");
        if (order.getOrderType() == null) order.setOrderType("매장");
        if (order.getStoreName() == null) order.setStoreName("");

        order.setOrderTime(new Date());

        // orders 테이블 insert
        orderMapper.insertOrder(order);

        // 상세 주문 insert
        List<OrderItemVO> items = order.getOrderItemList();
        if (items != null) {
            for (OrderItemVO item : items) {
                item.setOrderId(order.getOrderId());
                item.setMemberId(order.getUId());
            }
            orderMapper.insertOrderDetails(items);
        }

        // 전체 주문 불러오기
        OrderVO fullOrder = orderMapper.findOrderById(order.getOrderId(), order.getStoreName());

        // ⭐ 관리자에게 이벤트 발송 (sendToStore 사용)
        emitterStore.sendToStore(order.getStoreName(), "new-order", fullOrder);

        return fullOrder;
    }

    // 상태 업데이트 (완료/취소)
    @Override
    @Transactional
    public void updateOrderStatus(String status, Long orderId) {

        // 주문의 매장명 조회
        String storeName = orderMapper.findStoreNameByOrderId(orderId);

        // 주문 상태 변경
        orderMapper.updateOrderStatus(status, orderId);

        // 변경된 주문 불러오기
        OrderVO updatedOrder = orderMapper.findOrderById(orderId, storeName);

        // ⭐ 관리자에게 상태 변경 이벤트
        emitterStore.sendToStore(storeName, "order-update", updatedOrder);

        // ⭐ 고객에게 주문완료 SSE 전달
        if ("주문완료".equals(status)) {
            emitterStore.sendToUser(updatedOrder.getUId(), "order-complete", updatedOrder);

            // 스탬프 적립
            if (updatedOrder.getUId() != null) {
                rewardService.addStamps(updatedOrder.getUId(), updatedOrder.getTotalQuantity());
            }
        }
    }


    @Override
    public List<RecentOrderVO> getRecentOrders(String memberId) {
        return orderMapper.getRecentOrders(memberId);
    }

    @Override
    public List<RecentOrderVO> getAllOrders(String memberId) {
        return orderMapper.getAllOrders(memberId);
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        return orderMapper.selectOrderById(orderId);
    }
}
