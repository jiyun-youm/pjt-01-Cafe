package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.AdminService;
import com.miniproject.cafe.VO.AdminVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/login")
    public String adminLogin(HttpSession session, Model model, Authentication auth) {

        // ⭐ [수정 1] 로그인 했더라도 '관리자' 권한이 있는지 확인
        if (auth != null && auth.isAuthenticated()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                return "redirect:/admin/orders"; // 관리자만 통과
            }
            // 일반 회원이면 로그인 페이지를 그대로 보여줌 (혹은 메인으로 튕겨내도 됨)
        }

        if (session.getAttribute("loginError") != null) {
            model.addAttribute("loginError", session.getAttribute("loginError"));
            session.removeAttribute("loginError");
        }
        return "admin_login";
    }

    @GetMapping("/signup")
    public String adminSignup(HttpSession session, Model model) {
        Object msg = session.getAttribute("signupError");
        if (msg != null) {
            model.addAttribute("error", msg.toString());
            session.removeAttribute("signupError");
        }
        return "admin_signup";
    }

    @PostMapping("/joinForm")
    public String signup(AdminVO vo, HttpSession session) {
        try {
            adminService.register(vo);
        } catch (RuntimeException e) {
            session.setAttribute("signupError", e.getMessage());
            return "redirect:/admin/signup";
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/orders")
    public String adminOrders(HttpSession session, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        AdminVO admin = (AdminVO) session.getAttribute("admin");

        if (admin == null) {
            String loginId = principal.getName();
            admin = adminService.findById(loginId); // DB 조회

            if (admin != null) {
                session.setAttribute("admin", admin); // 세션 복구
                session.setAttribute("STORE_NAME", admin.getStoreName());
                System.out.println("♻️ 관리자 세션 자동 복구 완료: " + loginId);
            } else {
                // DB에도 없으면 로그아웃
                return "redirect:/admin/logout";
            }
        }

        model.addAttribute("storeName", admin.getStoreName());
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("activePage", "orders");

        return "admin_orders";
    }

    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam String id) {
        return adminService.checkId(id) > 0 ? "duplicate" : "available";
    }
}