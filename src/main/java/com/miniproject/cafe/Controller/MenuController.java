package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.MenuVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    private String getStoreNameFromSession(HttpSession session) {
        String storeName = (String) session.getAttribute("storeName");

        if (storeName == null || storeName.equals("selecting")) {
            return null;
        }

        return storeName;
    }

    /** ------------------ 커피 --------------------- **/
    @GetMapping("/coffee")
    public String coffee(Model model, HttpSession session) {

        String storeName = getStoreNameFromSession(session);

        if (storeName == null) {
            return "redirect:/home?msg=지점을+선택해주세요";
        }

        List<MenuVO> items = menuService.getMenuByStoreAndCategory(storeName, "커피");

        model.addAttribute("menuItems", items);
        model.addAttribute("storeName", storeName);

        return "menu/coffee";
    }

    /** ------------------ 음료 --------------------- **/
    @GetMapping("/beverage")
    public String beverage(Model model, HttpSession session) {

        String storeName = getStoreNameFromSession(session);

        if (storeName == null) {
            return "redirect:/home?msg=지점을+선택해주세요";
        }

        List<MenuVO> items = menuService.getMenuByStoreAndCategory(storeName, "음료");

        model.addAttribute("menuItems", items);
        model.addAttribute("storeName", storeName);

        return "menu/beverage";
    }

    /** ------------------ 푸드 --------------------- **/
    @GetMapping("/food")
    public String food(Model model, HttpSession session) {

        String storeName = getStoreNameFromSession(session);

        if (storeName == null) {
            return "redirect:/home?msg=지점을+선택해주세요";
        }

        List<MenuVO> items = menuService.getMenuByStoreAndCategory(storeName, "푸드");

        model.addAttribute("menuItems", items);
        model.addAttribute("storeName", storeName);

        return "menu/food";
    }

    /** ------------------ 티/에이드 --------------------- **/
    @GetMapping("/tea")
    public String tea(Model model, HttpSession session) {

        String storeName = getStoreNameFromSession(session);

        if (storeName == null) {
            return "redirect:/home?msg=지점을+선택해주세요";
        }

        List<MenuVO> items = menuService.getMenuByStoreAndCategory(storeName, "티");

        model.addAttribute("menuItems", items);
        model.addAttribute("storeName", storeName);

        return "menu/tea";
    }

    /** ------------------ 신메뉴 --------------------- **/
    @GetMapping("/newMenu")
    public String newMenu(Model model, HttpSession session) {

        String storeName = getStoreNameFromSession(session);

        if (storeName == null) {
            return "redirect:/home?msg=지점을+선택해주세요";
        }

        List<MenuVO> items = menuService.getMenuByStoreAndCategory(storeName, "신메뉴");

        model.addAttribute("menuItems", items);
        model.addAttribute("storeName", storeName);

        return "menu/newMenu";
    }
}
