package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.CustomUserDetailsService;
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


    @GetMapping("/")
    public String home(Model model, Authentication auth, Principal principal) {
        boolean isLoggedIn = (auth != null && auth.isAuthenticated());
        model.addAttribute("IS_LOGGED_IN", isLoggedIn);
        if(principal != null) {
            String memberId = principal.getName();
            List<RecentOrderVO> recentOrders = orderService.getRecentOrders(memberId);
            model.addAttribute("recentOrders", recentOrders);
            // ✅ reward 정보 추가
            RewardVO reward = rewardService.getReward(memberId);
            model.addAttribute("reward", reward);
        }
        return "main";
    }

    @GetMapping("/order_history")
    public String order_history(Model model, Principal principal) {

        if (principal != null) {
            String memberId = principal.getName();
            // 전체 주문 내역 조회
            List<RecentOrderVO> allOrders = orderService.getAllOrders(memberId);
            model.addAttribute("allOrders", allOrders);
        }

        return "order_history";
    }

    @GetMapping("/coffee")
    public String food() {
        return "redirect:/menu/coffee";
    }

    @GetMapping("/mypick")
    public String myPickPage(Model model, Authentication auth) {

        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = auth.getName();
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
    public String account(Authentication auth, HttpSession session, Model model) {
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/home/";
        }
        MemberVO member = (MemberVO) session.getAttribute("member");
        model.addAttribute("member", member);
        return "mypage";
    }
}
