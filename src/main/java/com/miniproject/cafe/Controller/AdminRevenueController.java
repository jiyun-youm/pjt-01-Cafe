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
import org.springframework.web.bind.annotation.ResponseBody;

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
        boolean isLoggedIn = session.getAttribute("admin") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("activePage", "revenue");

        // 로그인 안 되어 있으면 로그인 페이지로 이동
        if (!isLoggedIn) {
            return "redirect:/admin/login";
        }

        // 날짜가 없으면 오늘 날짜를 기본값으로 설정
        if(date == null || date.isEmpty()) {
            java.time.LocalDate today = java.time.LocalDate.now();
            date = today.toString(); // yyyy-MM-dd 형식
        }

        // 오늘 또는 선택된 날짜 기준으로 주문 조회
        List<AdminRevenueVO> orderDetailVO = adminRevenueService.getOrdersByDate(date);
        model.addAttribute("orderDetailVO", orderDetailVO);

        // HTML에서 input type="date"에 오늘 날짜를 기본값으로 표시
        model.addAttribute("selectedDate", date);

        return "admin_revenue";
    }

    @GetMapping("/revenue/orders")
    @ResponseBody
    public List<AdminRevenueVO> getRevenueOrders(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if(startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            return adminRevenueService.getOrdersByRange(startDate, endDate);
        } else if(date != null && !date.isEmpty()) {
            return adminRevenueService.getOrdersByDate(date);
        } else {
            return adminRevenueService.getAllOrders();
        }
    }




}
