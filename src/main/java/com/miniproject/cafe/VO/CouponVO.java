package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponVO {
    private int couponId;
    private String memberId;
    private String couponName;
    private String couponType;   // FREE_DRINK 등
    private LocalDate expireDate;
    private boolean used;        // 0 = 미사용, 1 = 사용, 2 = 만료
}
