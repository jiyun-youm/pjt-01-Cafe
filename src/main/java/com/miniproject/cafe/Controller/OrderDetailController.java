package com.miniproject.cafe.Controller;

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
    public String order_Detail(Model model, String id) {
        MenuVO menuDetail = orderDetailService.findById(id);
//        List<MenuVO> menuDetail=orderDetailService.getAllMenu();
        model.addAttribute("menuDetail", menuDetail);
        return "order_detail";
    }

    @PostMapping("/orderComplete")
    public String orderDetail(OrderDetailVO orderDetailVO) {
        orderDetailService.orderDetail(orderDetailVO);
        return "redirect:/home/order_detail";
    }

    @GetMapping("/main")
    public String home() {
        return "redirect:/home/main";
    }

    @GetMapping("/orderDetail")
    public String orderDetail() {
        return "/order_detail"; //
    }



}
