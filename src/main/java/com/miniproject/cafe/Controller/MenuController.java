package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/coffee")
    public String coffee(Model model) {
        List<MenuVO> menuItems = menuService.getMenuByCategory("커피");
        model.addAttribute("menuItems", menuItems);
        return "menu/coffee";
    }

    @GetMapping("/beverage")
    public String beverage(Model model) {
        List<MenuVO> menuItems = menuService.getMenuByCategory("음료");
        model.addAttribute("menuItems", menuItems);
        return "menu/beverage";
    }
    @GetMapping("/food")
    public String food(Model model) {
        List<MenuVO> menuItems = menuService.getMenuByCategory("푸드");
        model.addAttribute("menuItems", menuItems);
        return "menu/food";
    }

    @GetMapping("/tea")
    public String tea(Model model) {
        List<MenuVO> menuItems = menuService.getMenuByCategory("티");
        model.addAttribute("menuItems", menuItems);
        return "menu/tea";
    }

    @GetMapping("/newMenu")
    public String newMenu(Model model) {
        List<MenuVO> menuItems = menuService.getMenuByCategory("신메뉴");
        model.addAttribute("menuItems", menuItems);
        return "menu/newMenu";
    }

//    @GetMapping("/main")
//    public String home() {
//        return "redirect:/home/main";
//    }
//
//    @GetMapping("/mypick")
//    public String mypick() {
//        return "redirect:/home/mypick";
//    }
}
