package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {
    private long cartItemId; // 카트아이템 ID (PK)
    private long cartId; // 카트 ID (FK)
    private long menuOptionId; // 메뉴 옵션 ID (FK)
    private int quantity; // 개수
}
