package com.miniproject.cafe.Controller;

import com.miniproject.cafe.VO.OrderVO;
import com.miniproject.cafe.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // (추가)

//주문 생성 및 관리 REST API
@CrossOrigin(origins = "http://localhost:8383")
@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    //주문 생성
    @PostMapping("/create")
    public ResponseEntity<OrderVO> createOrder(@RequestBody OrderVO order) {
        System.out.println("🚀 [주문 접수] 매장명: " + order.getStoreName() + " / 주문자: " + order.getUId());
        try {
            OrderVO createdOrder = orderService.createOrder(order);
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    //주문 목록 조회
    @GetMapping("/admin-list")
    public ResponseEntity<List<OrderVO>> getAdminOrderList(
            @RequestParam("storeName") String storeName) {

        List<OrderVO> orders = orderService.getOrdersByStore(storeName);
        return ResponseEntity.ok(orders);
    }

    //주문 상태 업데이트 (완료, 취소)
    @PutMapping("/status/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload) {

        String newStatus = payload.get("status"); // "주문완료" 또는 "주문취소"

        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            orderService.updateOrderStatus(newStatus, orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}