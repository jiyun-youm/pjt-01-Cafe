package com.miniproject.cafe.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class HomeController {

    @GetMapping("/")
    public String home(Model model, HttpSession session) {

        String oauthError = (String) session.getAttribute("oauthErrorMessage");
        if (oauthError != null) {
            model.addAttribute("oauthErrorMessage", oauthError);
            session.removeAttribute("oauthErrorMessage");
        }
        String successType = (String) session.getAttribute("loginSuccessType");
        String userName = (String) session.getAttribute("loginMemberName");

        if (successType != null) {
            model.addAttribute("loginSuccessType", successType);
            model.addAttribute("loginMemberName", userName);

            session.removeAttribute("loginSuccessType");
            session.removeAttribute("loginMemberName");
        }
        return "main";
    }

    @GetMapping("/order_history")
    public String order_history() {
        return "order_history";
    }

    @GetMapping("/coffee")
    public String food() {
        return "redirect:/menu/coffee";
    }

    @GetMapping("/mypick")
    public String myPickPage() {
        return "mypick";
    }
}
