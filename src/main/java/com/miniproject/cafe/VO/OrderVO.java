package com.miniproject.cafe.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderVO {

    private Long orderId;      // 주문 번호 (#0001)
    private Date orderTime;     // 주문 시간
    private int totalQuantity;    // 총 수량
    private int totalPrice;       // 총 금액
    private String orderType; // 주문 유형 (매장, 포장, 배달)

    @JsonProperty("storeName")
    private String storeName; //매장명
    @JsonProperty("uId")
    private String uId; //사용자아이디

    private String username; //사용자명
    
    private String orderStatus;

    private List<OrderItemVO> orderItemList;

    private Long dailyOrderNum;
}