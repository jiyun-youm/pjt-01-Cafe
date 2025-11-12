package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {
    private long cartId; // 카트 ID (PK)
    private int memberId; // 유저 ID (FK, UK)
    private int totalPrice; // 장바구니 총 가격
}