package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.CouponMapper;
import com.miniproject.cafe.Service.CouponService;
import com.miniproject.cafe.VO.CouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Override
    public List<CouponVO> getCouponsByUser(String memberId) {
        return couponMapper.getCouponsByUser(memberId);
    }

    @Override
    public void insertCoupon(CouponVO coupon) {
        couponMapper.insertCoupon(coupon);
    }

    @Override
    public void useCoupon(int couponId) {
        couponMapper.markUsed(couponId);
    }

    ///스탬프 10개 도달 시 자동 쿠폰 생성
    @Override
    public void createFreeDrinkCoupon(String memberId) {
        CouponVO coupon = new CouponVO();
        coupon.setMemberId(memberId);
        coupon.setCouponName("무료 제조음료 쿠폰");
        coupon.setCouponType("FREE_DRINK");
        coupon.setExpireDate(LocalDate.now().plusMonths(1)); // 1개월 유효기간
        coupon.setUsed(false);

        couponMapper.insertCoupon(coupon);
    }
}
