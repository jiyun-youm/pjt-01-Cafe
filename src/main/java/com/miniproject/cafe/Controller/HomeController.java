package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Service.CouponService;
import com.miniproject.cafe.Service.OrderService;
import com.miniproject.cafe.Service.RewardService;
import com.miniproject.cafe.Service.UserLikeService;
import com.miniproject.cafe.VO.MemberVO;
import com.miniproject.cafe.VO.MenuVO;
import com.miniproject.cafe.VO.RecentOrderVO;
import com.miniproject.cafe.VO.RewardVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final UserLikeService userLikeService;
    private final OrderService orderService;
    private final RewardService rewardService;
    private final CouponService couponService;
    private final MemberMapper memberMapper;

    // 로그인 체크 유틸리티 메서드
    private boolean isLoggedIn(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    private String getMemberId(Authentication auth) {
        return isLoggedIn(auth) ? auth.getName() : null;
    }

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        boolean isLoggedIn = isLoggedIn(auth);
        model.addAttribute("IS_LOGGED_IN", isLoggedIn);

        if(isLoggedIn) {
            String memberId = getMemberId(auth);
            List<RecentOrderVO> recentOrders = orderService.getRecentOrders(memberId);
            model.addAttribute("recentOrders", recentOrders);
            RewardVO reward = rewardService.getReward(memberId);
            model.addAttribute("reward", reward);
            int couponCount = couponService.getCouponsByUser(memberId).size();
            model.addAttribute("couponCount", couponCount);
        }

        return "main";
    }

    @GetMapping("/order_history")
    public String order_history(Model model, Authentication auth) {
        boolean isLoggedIn = isLoggedIn(auth);
        model.addAttribute("IS_LOGGED_IN", isLoggedIn);

        if (!isLoggedIn(auth)) {
            return "redirect:/home/";
        }

        String memberId = getMemberId(auth);
        List<RecentOrderVO> allOrders = orderService.getAllOrders(memberId);
        model.addAttribute("allOrders", allOrders);

        return "order_history";
    }

    @GetMapping("/coffee")
    public String food() {
        return "redirect:/menu/coffee";
    }

    @GetMapping("/mypick")
    public String myPickPage(Model model, Authentication auth) {
        boolean isLoggedIn = isLoggedIn(auth);
        model.addAttribute("IS_LOGGED_IN", isLoggedIn);

        if(!isLoggedIn(auth)) {
            return "redirect:/home/";
        }

        String userId = getMemberId(auth);
        List<MenuVO> likedMenus = userLikeService.getLikedMenus(userId);
        model.addAttribute("likedMenus", likedMenus);

        return "mypick";
    }

    @PostMapping("/saveRegion")
    @ResponseBody
    public String saveRegion(@RequestBody Map<String, String> data, HttpSession session) {
        String storeName = data.get("region");

        if (storeName == null || storeName.equals("selecting")) {
            session.removeAttribute("storeName");
            return "cleared";
        }

        session.setAttribute("storeName", storeName);
        return "saved";
    }

    @GetMapping("/getRegion")
    @ResponseBody
    public String getRegion(HttpSession session) {
        Object storeName = session.getAttribute("storeName");
        return storeName != null ? storeName.toString() : null;
    }

    @GetMapping("/account")
    public String account(Authentication auth, Model model) {

        // remember-me 포함 모든 인증 처리
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/home/login";
        }

        String email = auth.getName();
        MemberVO member = memberMapper.findByEmail(email);

        if (member == null) {
            return "redirect:/home/login";
        }

        model.addAttribute("IS_LOGGED_IN", true);
        model.addAttribute("member", member);

        return "mypage";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}