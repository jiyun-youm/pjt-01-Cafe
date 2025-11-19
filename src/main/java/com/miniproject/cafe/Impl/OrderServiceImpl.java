package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Emitter.SseEmitterStore;
import com.miniproject.cafe.Mapper.OrderMapper;
import com.miniproject.cafe.Service.OrderService;
import com.miniproject.cafe.VO.OrderItemVO;
import com.miniproject.cafe.VO.OrderVO;
import com.miniproject.cafe.VO.RecentOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // (추가)

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SseEmitterStore emitterStore;

    @Override
    public List<OrderVO> getOrdersByStore(String storeName) {
        return orderMapper.findOrdersByStore(storeName);
    }

    //신규 주문
    @Override
    @Transactional // 트랜잭션 필수: 주문서 생성과 상세 메뉴 저장이 모두 성공해야 커밋됨
    public OrderVO createOrder(OrderVO order) {

        // 1. 기본값 세팅
        if (order.getOrderStatus() == null) order.setOrderStatus("주문접수");
        if (order.getOrderType() == null) order.setOrderType("매장");
        if (order.getStoreName() == null) order.setStoreName("");


        // 2. orders 테이블에 저장 (여기서 DB가 생성한 orderId가 order 객체에 담김)
        order.setOrderTime(new java.util.Date());
        orderMapper.insertOrder(order);

        // 3. 상세 메뉴(Item)들이 있다면, 방금 생성된 주문번호(orderId)를 넣고 저장
        List<OrderItemVO> items = order.getOrderItemList();

        if (items != null && !items.isEmpty()) {
            String memberId = order.getUId();
            for (OrderItemVO item : items) {
                item.setOrderId(order.getOrderId());
                item.setMemberId(memberId);
            }
            orderMapper.insertOrderDetails(items);
        }
        // 새 주문을 해당 매장(storeName)의 관리자에게 SSE push
        OrderVO fullOrder = orderMapper.findOrderById(order.getOrderId(), order.getStoreName());

        // 4) ★ SSE로 보내는 것은 fullOrder이어야 한다 ★
        emitterStore.sendToStore(order.getStoreName(), fullOrder);
        return fullOrder;
    }

    //주문 업데이트 (완료, 취소)
    @Override
    @Transactional
    public void updateOrderStatus(String status, Long orderId) {

        // 1) 해당 주문의 매장 이름 조회
        String storeName = orderMapper.findStoreNameByOrderId(orderId);

        // 2) 상태 업데이트
        orderMapper.updateOrderStatus(status, orderId);

        // 3) 매장 필터를 적용해 변경된 주문 정보 조회
        if (storeName != null && !storeName.isEmpty()) {
            OrderVO updatedOrder = orderMapper.findOrderById(orderId, storeName);

            // 4) SSE push (해당 매장 관리자에게만)
            if (updatedOrder != null) {
                emitterStore.sendToStore(storeName, updatedOrder);
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
}