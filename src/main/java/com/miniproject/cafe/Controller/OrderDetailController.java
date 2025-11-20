package com.miniproject.cafe.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.miniproject.cafe.Impl.MenuServiceImpl;
import com.miniproject.cafe.Service.OrderDetailService;
import com.miniproject.cafe.VO.MenuVO;
import com.miniproject.cafe.VO.OrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.*;
import java.util.*;
import java.util.List;

@Controller
@RequestMapping("/home")
public class OrderDetailController {

    @Autowired
    @Qualifier("OrderDetailService")
    private OrderDetailService orderDetailService;

    @Autowired
    private MenuServiceImpl menuServiceImpl;


    @GetMapping("/order_detail")
    public String order_Detail(HttpSession session, Model model,
                               @RequestParam("id") String id) {

        String storeName = (String) session.getAttribute("storeName");

        if (storeName == null || storeName.trim().isEmpty()) {
            return "redirect:/home/";
        }

        MenuVO menu = orderDetailService.findById(id);
        if(menu == null) {
            return "redirect:/home/";
        }

        model.addAttribute("menu", menu);
        model.addAttribute("storeName", storeName);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isLoggedIn =
                auth != null &&
                        auth.isAuthenticated() &&
                        !(auth instanceof AnonymousAuthenticationToken);

        model.addAttribute("IS_LOGGED_IN", isLoggedIn);

        if (isLoggedIn) {
            String loginUserId = auth.getName();    // = member.id
            model.addAttribute("LOGIN_USER_ID", loginUserId);
        }

        return "order_detail";
    }


    @PostMapping("/orderComplete")
    public String orderDetail(OrderDetailVO orderDetailVO) {
        orderDetailService.orderDetail(orderDetailVO);
        return "redirect:/home/order_detail";
    }
}
