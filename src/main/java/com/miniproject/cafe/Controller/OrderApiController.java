package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Emitter.SseEmitterStore;
import com.miniproject.cafe.VO.OrderVO;
import com.miniproject.cafe.Service.OrderService;
import com.miniproject.cafe.VO.RecentOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {

        try {
            String status = body.get("status");
            OrderVO order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("주문을 찾을 수 없습니다.");
            }

            if (!order.getOrderStatus().equals("주문접수")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("이미 처리된 주문입니다.");
            }

            orderService.updateOrderStatus(status, orderId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류");
        }
    }

    @GetMapping("/user-list")
    public ResponseEntity<List<RecentOrderVO>> getUserOrders(
            @RequestParam("memberId") String memberId) {

        List<RecentOrderVO> list = orderService.getAllOrders(memberId);
        return ResponseEntity.ok(list);
    }

}