package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.RewardVO;

public interface RewardService {
    RewardVO getReward(String memberId);
    void addStamps(String memberId, int quantity); // 스탬프 증가
}
