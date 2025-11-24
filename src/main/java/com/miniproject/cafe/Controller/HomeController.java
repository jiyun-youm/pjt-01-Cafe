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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    private MemberVO getMemberFromAuth(Authentication auth, HttpSession session) {
        // 1. 로그인 안 된 상태면 null 반환
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        // 2. 세션에서 먼저 찾기 (가장 빠름)
        MemberVO member = (MemberVO) session.getAttribute("member");

        // 3. 세션에 없으면 DB에서 다시 조회 (Remember-Me 복구)
        if (member == null) {
            String loginId = null;
            Object principal = auth.getPrincipal();

            if (principal instanceof UserDetails) {
                loginId = ((UserDetails) principal).getUsername(); // 일반/소셜 로그인 객체
            } else {
                loginId = auth.getName(); // 문자열 ID인 경우
            }

            // DB 조회
            member = memberMapper.findByEmail(loginId);

            // 4. 찾았으면 세션에 다시 저장 (다음 요청부터는 DB 조회 안 함)
            if (member != null) {
                session.setAttribute("member", member);
                session.setAttribute("LOGIN_USER_ID", member.getId());
                // System.out.println("✅ [HomeController] 사용자 세션 복구 완료: " + member.getEmail());
            }
        }
        return member;
    }

    @GetMapping("/")
    public String home(Model model, Authentication auth, HttpSession session) {

        // 헬퍼 메서드를 통해 회원 정보 가져오기 (세션 없으면 DB에서 가져옴)
        MemberVO member = getMemberFromAuth(auth, session);
        boolean isLoggedIn = (member != null);

        model.addAttribute("IS_LOGGED_IN", isLoggedIn);

        if (isLoggedIn) {
            String memberId = member.getId();

            // 데이터 조회해서 모델에 담기 (이제 null 에러 안 남)
            model.addAttribute("recentOrders", orderService.getRecentOrders(memberId));
            model.addAttribute("reward", rewardService.getReward(memberId));
            model.addAttribute("couponCount", couponService.getCouponsByUser(memberId).size());
            model.addAttribute("member", member);
        }

        return "main";
    }

    @GetMapping("/order_history")
    public String order_history(Model model, Authentication auth, HttpSession session) {

        MemberVO member = getMemberFromAuth(auth, session);

        // 로그인이 풀렸거나 회원 정보가 없으면 로그인 페이지로
        if (member == null) {
            return "redirect:/home/login";
        }

        model.addAttribute("IS_LOGGED_IN", true);
        model.addAttribute("allOrders", orderService.getAllOrders(member.getId()));

        return "order_history";
    }

    @GetMapping("/mypick")
    public String myPickPage(Model model, Authentication auth, HttpSession session) {

        MemberVO member = getMemberFromAuth(auth, session);

        if (member == null) {
            return "redirect:/home/login";
        }

        model.addAttribute("IS_LOGGED_IN", true);
        model.addAttribute("likedMenus", userLikeService.getLikedMenus(member.getId()));

        return "mypick";
    }

    @GetMapping("/account")
    public String account(Authentication auth, Model model, HttpSession session) {

        MemberVO member = getMemberFromAuth(auth, session);

        // 여기서 member가 null이 아니면(세션 복구됨) 튕기지 않고 마이페이지로 감
        if (member == null) {
            return "redirect:/home/login";
        }

        model.addAttribute("IS_LOGGED_IN", true);
        model.addAttribute("member", member);

        return "mypage";
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

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}