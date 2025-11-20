package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardVO {
    private String memberId;  // 회원 ID
    private int stamps;       // 스탬프 수
    private int coupons;      // 쿠폰 수
}
