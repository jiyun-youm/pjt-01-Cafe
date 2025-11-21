package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.CouponVO;

import java.util.List;

public interface CouponService {

    // 보유 중인 쿠폰 조회
    List<CouponVO> getCouponsByUser(String memberId);

    // 특정 쿠폰 저장 (직접 INSERT)
    void insertCoupon(CouponVO coupon);

    // 무료 음료 쿠폰 자동 생성 (스탬프 10개 시 호출)
    void createFreeDrinkCoupon(String memberId);

    // 쿠폰 사용 처리
    void useCoupon(int couponId);
}
