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

import java.util.Collections;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final RememberMeServices adminRememberMeServices;

    // [중요] 생성자 주입 시 @Qualifier를 사용하여 관리자용 RememberMeServices를 지정합니다.
    public AdminController(AdminService adminService,
                           @Qualifier("adminRememberMeServices") RememberMeServices adminRememberMeServices) {
        this.adminService = adminService;
        this.adminRememberMeServices = adminRememberMeServices;
    }

    private String sanitize(String msg) {
        if (msg == null) return "";
        return msg.replaceAll("[\r\n]", "");
    }

    @GetMapping("/orders")
    public String adminOrders(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("activePage", "orders");
        return "admin_orders";
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

    @GetMapping("/login")
    public String adminLogin(HttpSession session, Model model) {
        if (session.getAttribute("loginError") != null) {
            model.addAttribute("loginError", session.getAttribute("loginError"));
            session.removeAttribute("loginError");
        }
        return "admin_login";
    }

    // [관리자 로그인 처리]
    @PostMapping("/login")
    public String login(AdminVO vo,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        HttpSession session,
                        RedirectAttributes ra) {

        // 1. 서비스 호출 (ID/PW 검증)
        AdminVO loginAdmin = adminService.login(vo);

        if (loginAdmin != null) {
            // 2. 세션 저장
            session.setAttribute("admin", loginAdmin);

            // 3. Spring Security 인증 객체 생성 및 컨텍스트 설정
            // (권한은 ROLE_ADMIN 부여)
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    loginAdmin.getId(),
                    null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(auth);
            SecurityContextHolder.setContext(sc);

            // 4. [핵심] Remember-Me 쿠키 생성 (자동 로그인 체크 시)
            adminRememberMeServices.loginSuccess(request, response, auth);

            return "redirect:/admin/orders";
        } else {
            // 로그인 실패
            session.setAttribute("loginError", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam String id) {
        int count = adminService.checkId(id);
        return count > 0 ? "duplicate" : "available";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}