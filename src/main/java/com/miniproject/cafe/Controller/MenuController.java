package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.MenuVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    private String getValidRegion(String region) {
        if (region == null || region.equals("selecting")) {
            return "default";  // 기본 지점
        }
        return region;
    }

    @GetMapping("/coffee")
    public String coffee(@RequestParam(required = false) String region, Model model) {

        region = getValidRegion(region);

        List<MenuVO> items = menuService.getMenuByRegionAndCategory(region, "커피");

        model.addAttribute("menuItems", items);
        model.addAttribute("region", region);

        return "menu/coffee";
    }

    @GetMapping("/beverage")
    public String beverage(@RequestParam(required = false) String region, Model model) {

        region = getValidRegion(region);

        List<MenuVO> items = menuService.getMenuByRegionAndCategory(region, "음료");

        model.addAttribute("menuItems", items);
        model.addAttribute("region", region);

        return "menu/beverage";
    }

    @GetMapping("/food")
    public String food(@RequestParam(required = false) String region, Model model) {

        region = getValidRegion(region);

        List<MenuVO> items = menuService.getMenuByRegionAndCategory(region, "푸드");

        model.addAttribute("menuItems", items);
        model.addAttribute("region", region);

        return "menu/food";
    }

    @GetMapping("/tea")
    public String tea(@RequestParam(required = false) String region, Model model) {

        region = getValidRegion(region);

        List<MenuVO> items = menuService.getMenuByRegionAndCategory(region, "티");

        model.addAttribute("menuItems", items);
        model.addAttribute("region", region);

        return "menu/tea";
    }

    @GetMapping("/newMenu")
    public String newMenu(@RequestParam(required = false) String region, Model model) {

        region = getValidRegion(region);

        List<MenuVO> items = menuService.getMenuByRegionAndCategory(region, "신메뉴");

        model.addAttribute("menuItems", items);
        model.addAttribute("region", region);

        return "menu/newMenu";
    }
}
