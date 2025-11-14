package com.miniproject.cafe.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/orders")
    public String adminOrders() {
        return "admin_orders";
    }

    @GetMapping("/signup")
    public String adminSignup() {
        return "admin_signup";
    }

    @GetMapping("/login")
    public String adminLogin() {
        return "admin_login";
    }
}
