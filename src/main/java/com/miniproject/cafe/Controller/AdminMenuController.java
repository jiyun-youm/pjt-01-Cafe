package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.MenuVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminMenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping("/menu")
    public String menuManagement(Model model, HttpSession session) {

        String storeName = (String) session.getAttribute("storeName");

        if(storeName == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("isLoggedIn", session.getAttribute("adminId") != null);

        List<MenuVO> menuList = menuService.getMenuByStore(storeName);

        model.addAttribute("menuList", menuList);
        model.addAttribute("storeName", storeName);

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
    public String insertMenu(MenuVO vo,
                             @RequestParam(value = "menuImgFile", required = false) MultipartFile file,
                             HttpSession session) {

        String storeName = (String) session.getAttribute("storeName");
        if (storeName == null) throw new RuntimeException("지점 정보가 없습니다. 다시 로그인해주세요.");

        vo.setStoreName(storeName);

        String prefix = getStorePrefix(storeName);
        String lastId = menuService.getLastMenuIdByStore(storeName);
        String newMenuId = generateNextId(prefix, lastId);
        vo.setMenuId(newMenuId);

        // 이미지 처리
        if (file != null && !file.isEmpty()) {
            String fileName = newMenuId + "_" + file.getOriginalFilename();
            String filePath = "C:/upload/menuImg/";

            try {
                java.io.File dir = new java.io.File(filePath);
                if (!dir.exists()) dir.mkdirs();

                file.transferTo(new java.io.File(filePath + fileName));
                vo.setMenuImg(fileName);
            } catch (Exception e) {
                e.printStackTrace();
                vo.setMenuImg("default.png");
            }
        } else {
            vo.setMenuImg("default.png");
        }

        if (vo.getSalesStatus() == null) vo.setSalesStatus("판매중");

        menuService.insertMenu(vo);
        return "redirect:/admin/menu";
    }


    private String getStorePrefix(String storeName) {
        switch (storeName) {
            case "강남중앙점": return "GN";
            case "역삼중앙점": return "YS";
            case "선릉중앙점": return "SL";
            default: return "MN";
        }
    }

    private String generateNextId(String prefix, String lastId) {
        if (lastId == null) {
            return prefix + "001";
        }
        String numberPart = lastId.substring(prefix.length());
        int nextNum = Integer.parseInt(numberPart) + 1;
        return prefix + String.format("%03d", nextNum);
    }

    // 개별 삭제 API
    @DeleteMapping("/deleteMenu/{id}")
    @ResponseBody
    public String deleteMenu(@PathVariable("id") String menuId, HttpSession session) {
        String storeName = (String) session.getAttribute("storeName");
        menuService.deleteMenuByStore(menuId, storeName);
        return "success";
    }

    // 선택 삭제 API
    @PostMapping("/deleteMenuBatch")
    @ResponseBody
    public String deleteMenuBatch(@RequestBody List<String> ids, HttpSession session) {

        String storeName = (String) session.getAttribute("storeName");

        for (String id : ids) {
            menuService.deleteMenuByStore(id, storeName);
        }
        return "success";
    }
}
