package com.miniproject.cafe.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class HomeController {

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        boolean isLoggedIn = (auth != null && auth.isAuthenticated());
        model.addAttribute("IS_LOGGED_IN", isLoggedIn);
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
