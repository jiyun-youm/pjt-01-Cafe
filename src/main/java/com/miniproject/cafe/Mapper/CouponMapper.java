package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.CouponVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponMapper {

    // 유저 보유 쿠폰 목록 조회
    List<CouponVO> getCouponsByUser(@Param("memberId") String memberId);

    // 쿠폰 추가 (발급)
    void insertCoupon(CouponVO coupon);

    // 사용 처리
    void markUsed(@Param("couponId") int couponId);
}
