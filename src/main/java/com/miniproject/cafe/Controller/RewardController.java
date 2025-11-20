package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.RewardService;
import com.miniproject.cafe.VO.RewardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reward")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping("/{memberId}")
    public RewardVO getReward(@PathVariable String memberId) {
        RewardVO reward = rewardService.getReward(memberId);
        return reward != null ? reward : new RewardVO(memberId, 0, 0);
    }
}
