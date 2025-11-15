package com.miniproject.cafe.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminMenuController {
    @GetMapping("/menu")
    public String menuManagement() {
        return "admin_menu_management";
    }
}
