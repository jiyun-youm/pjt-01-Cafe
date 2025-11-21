package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.RewardMapper;
import com.miniproject.cafe.Service.CouponService;
import com.miniproject.cafe.Service.RewardService;
import com.miniproject.cafe.VO.RewardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardServiceImpl implements RewardService {

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private CouponService couponService;

    @Override
    public RewardVO getReward(String memberId) {
        return rewardMapper.findByMemberId(memberId);
    }

    @Override
    @Transactional
    public void addStamps(String memberId, int quantity) {

        // 기존 리워드 조회
        RewardVO reward = rewardMapper.findByMemberId(memberId);

        if (reward == null) {
            // 첫 가입
            reward = new RewardVO();
            reward.setMemberId(memberId);
            reward.setStamps(0);
            reward.setCoupons(0);

            rewardMapper.insertReward(reward);
        }

        // 스탬프 증가
        int total = reward.getStamps() + quantity;

        // 생성할 쿠폰 개수
        int couponCount = total / 10;

        // 남은 스탬프
        reward.setStamps(total % 10);

        // reward DB 업데이트
        rewardMapper.updateReward(reward);

        // 쿠폰 발급
        for (int i = 0; i < couponCount; i++) {
            couponService.createFreeDrinkCoupon(memberId);
        }
    }
}
