package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.AdminRevenueService;
import com.miniproject.cafe.VO.AdminRevenueVO;
import com.miniproject.cafe.VO.OrderDetailVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminRevenueController {
    @Autowired
    private AdminRevenueService adminRevenueService;

    @GetMapping("/revenue")
    public String adminRevenue(HttpSession session, Model model,
                               @RequestParam(required=false) String date) {
        // 로그인 상태 체크
        boolean isLoggedIn = session.getAttribute("adminId") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        // 로그인 안 되어 있으면 로그인 페이지로 이동
        if (!isLoggedIn) {
            return "redirect:/admin/login";
        }

        // 주문 조회
        if(date == null || date.isEmpty()) {
            List<AdminRevenueVO> orderDetailVO = adminRevenueService.getAllOrders();
            model.addAttribute("orderDetailVO", orderDetailVO);
        } else {
            List<AdminRevenueVO> orderDetailVO = adminRevenueService.getOrdersByDate(date);
            model.addAttribute("orderDetailVO", orderDetailVO);
        }

        return "admin_revenue";
    }


}
