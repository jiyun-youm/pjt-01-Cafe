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
    public String adminRevenue(Model model,
                               @RequestParam(required=false) String date) {
        if(date == null || date.isEmpty()) {
            List<AdminRevenueVO> orderDetailVO=adminRevenueService.getAllOrders();
            model.addAttribute("orderDetailVO", orderDetailVO);
            return "admin_revenue";   // 전체 조회
        } else {
            List<AdminRevenueVO> orderDetailVO=adminRevenueService.getOrdersByDate(date);
            model.addAttribute("orderDetailVO", orderDetailVO);
            return "admin_revenue";  // 특정 날짜 조회
        }
    }

}
