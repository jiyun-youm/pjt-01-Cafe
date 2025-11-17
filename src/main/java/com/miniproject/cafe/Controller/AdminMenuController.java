package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminMenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping("/menu")
    public String menuManagement(Model model) {

        List<MenuVO> menuList = menuService.getAllMenu();
        System.out.println("==== 메뉴 조회 결과 ====");
        menuList.forEach(m -> System.out.println(m.getMenuId() + " / " + m.getMenuName() + " / " + m.getCategory()));

        model.addAttribute("menuList", menuList);
        return "admin_menu_management";
    }

    // 메뉴 등록 페이지
    @GetMapping("/insertMenu")
    public String insertMenuPage() {
        return "admin_insert_menu";
    }

    // 메뉴 등록 처리
    @PostMapping("/insertMenu")
    public String insertMenu(MenuVO vo) {

        System.out.println("===== 신규 메뉴 등록 요청 =====");
        System.out.println("메뉴명: " + vo.getMenuName());
        System.out.println("가격: " + vo.getMenuPrice());
        System.out.println("카테고리: " + vo.getCategory());
        System.out.println("설명: " + vo.getMenuDefinition());

        menuService.insertMenu(vo);

        return "redirect:/admin/menu";
    }

    // 개별 삭제 API
    @DeleteMapping("/deleteMenu/{id}")
    @ResponseBody
    public String deleteMenu(@PathVariable("id") String menuId) {
        menuService.deleteMenu(menuId);
        return "success";
    }

    // 선택 삭제 API
    @PostMapping("/deleteMenuBatch")
    @ResponseBody
    public String deleteMenuBatch(@RequestBody List<String> ids) {
        for (String id : ids) {
            menuService.deleteMenu(id);
        }
        return "success";
    }
}
