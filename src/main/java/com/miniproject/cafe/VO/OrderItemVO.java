package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {
    private Long orderItemId;
    private Long orderId;
    private String menuId;
    private String menuItemName; //메뉴 이름
    private int quantity; //수량

    private Long optionId;

    private String temp;        // ICE / HOT
    private int tumbler;        // 0 or 1
    private int shot;           // 샷 추가 수량
    private int vanillaSyrup;   // 시럽 추가 수량
    private int whippedCream;   // 휘핑 추가 수량
    private String memberId;
}
