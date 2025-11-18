package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.AdminService;
import com.miniproject.cafe.VO.AdminVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 개행 문자 제거하는 sanitizing 메서드 추가
    private String sanitize(String msg) {
        if (msg == null) return "";
        return msg.replaceAll("[\r\n]", "");  // CR/LF 제거
    }

    @GetMapping("/orders")
    public String adminOrders(HttpSession session, Model model) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/admin/login";
        }
        //로그인 상태 전달
        model.addAttribute("isLoggedIn", session.getAttribute("adminId") != null);
        return "admin_orders";
    }

    // 회원가입 화면
    @GetMapping("/signup")
    public String adminSignup(HttpSession session, Model model) {
        Object msg = session.getAttribute("signupError");
        if (msg != null) {
            model.addAttribute("error", msg.toString());
            session.removeAttribute("signupError");   // 한 번만 표시
        }
        return "admin_signup";
    }

    // 회원가입 처리
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

    // 로그인 화면
    @GetMapping("/login")
    public String adminLogin(HttpSession session, Model model) {
        // 세션에 adminId가 있으면 로그인 상태
        boolean isLoggedIn = session.getAttribute("adminId") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "admin_login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String id,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes ra) {

        try {
            AdminVO admin = adminService.login(id, password);
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("storeName", admin.getStoreName());

        } catch (RuntimeException e) {
            ra.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/admin/login";
        }

        return "redirect:/admin/orders";
    }

    // 아이디 중복 체크 API
    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam String id) {
        int count = adminService.checkId(id);
        return count > 0 ? "duplicate" : "available";
    }

    //로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 초기화
        return "redirect:/admin/login";
    }
}
