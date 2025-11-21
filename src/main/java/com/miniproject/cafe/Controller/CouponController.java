package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.CouponService;
import com.miniproject.cafe.Service.RewardService;
import com.miniproject.cafe.VO.CouponVO;
import com.miniproject.cafe.VO.RewardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private RewardService rewardService;

    @GetMapping("/home/coupon")
    public String couponPage(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName();  // 로그인 중이면 항상 존재

        List<CouponVO> coupons = couponService.getCouponsByUser(memberId);
        RewardVO reward = rewardService.getReward(memberId);

        model.addAttribute("coupons", coupons);
        model.addAttribute("reward", reward);

        return "coupon";
    }

}
