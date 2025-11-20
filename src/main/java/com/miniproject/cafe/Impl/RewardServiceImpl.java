package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.RewardMapper;
import com.miniproject.cafe.Service.RewardService;
import com.miniproject.cafe.VO.RewardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardServiceImpl implements RewardService {

    @Autowired
    private RewardMapper rewardMapper;

    @Override
    public RewardVO getReward(String memberId) {
        return rewardMapper.findByMemberId(memberId);
    }

    @Override
    @Transactional
    public void addStamps(String memberId, int quantity) {
        RewardVO reward = rewardMapper.findByMemberId(memberId);

        if (reward == null) {
            // 처음이면 새로 생성
            reward = new RewardVO();
            reward.setMemberId(memberId);
            reward.setStamps(quantity);
            reward.setCoupons(0);

            // 10 이상이면 쿠폰으로 변환
            int coupons = reward.getStamps() / 10;
            reward.setCoupons(coupons);
            reward.setStamps(reward.getStamps() % 10);

            rewardMapper.insertReward(reward);
        } else {
            // 기존이면 증가
            int newStamps = reward.getStamps() + quantity;
            int newCoupons = reward.getCoupons() + (newStamps / 10);
            newStamps = newStamps % 10;

            reward.setStamps(newStamps);
            reward.setCoupons(newCoupons);

            rewardMapper.updateReward(reward);
        }
    }
}
