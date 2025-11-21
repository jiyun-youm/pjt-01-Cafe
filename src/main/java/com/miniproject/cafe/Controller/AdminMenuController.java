package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.AdminVO;
import com.miniproject.cafe.VO.MenuVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminMenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping("/menu")
    public String menuManagement(Model model, HttpSession session) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        String storeName = admin.getStoreName();

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("activePage", "menu");

        List<MenuVO> menuList = menuService.getMenuByStore(storeName);

        model.addAttribute("menuList", menuList);
        model.addAttribute("storeName", storeName);

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
                             @RequestParam("temperature") String temperature,
                             HttpSession session) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) throw new RuntimeException("관리자 로그인 정보가 없습니다.");

        String storeName = admin.getStoreName();
        vo.setStoreName(storeName);

        // 메뉴 ID 생성
        String prefix = getStorePrefix(storeName);
        String lastId = menuService.getLastMenuIdByStore(storeName);
        String newMenuId = generateNextId(prefix, lastId);
        vo.setMenuId(newMenuId);

        // temperature → DB hotAvailable
        vo.setHotAvailable("AVAILABLE".equals(temperature) ? 1 : 0);

        // 이미지 처리
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = java.util.UUID.randomUUID().toString() + ext;

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

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "fail";

        String storeName = admin.getStoreName();
        menuService.deleteMenuByStore(menuId, storeName);

        return "success";
    }

    // 선택 삭제 API
    @PostMapping("/deleteMenuBatch")
    @ResponseBody
    public String deleteMenuBatch(@RequestBody List<String> ids, HttpSession session) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "fail";

        String storeName = admin.getStoreName();

        for (String id : ids) {
            menuService.deleteMenuByStore(id, storeName);
        }

        return "success";
    }

    @PostMapping("/updateStatus")
    @ResponseBody
    public String updateMenuStatus(@RequestBody Map<String, String> data, HttpSession session) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "fail";

        String menuId = data.get("menuId");
        String status = data.get("status");
        String storeName = admin.getStoreName();

        menuService.updateSalesStatus(menuId, storeName, status);

        return "success";
    }

    @GetMapping("/updateMenu/{menuId}")
    public String updateMenuPage(@PathVariable String menuId, Model model, HttpSession session) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/login";

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("activePage", "menu");

        MenuVO menu = menuService.getMenuById(menuId);

        model.addAttribute("menu", menu);
        model.addAttribute("menuList", menuService.getMenuByStore(admin.getStoreName()));

        Boolean updated = (Boolean) session.getAttribute("updateSuccess");
        model.addAttribute("updateSuccess", updated);

        session.removeAttribute("updateSuccess");

        return "admin_menu_management";
    }




    @PostMapping("/updateMenu")
    public String updateMenu(
            MenuVO vo,
            @RequestParam(value="menuImgFile", required=false) MultipartFile file,
            @RequestParam("temperature") String temperature,
            HttpSession session
    ) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) throw new RuntimeException("관리자 로그인 필요");

        vo.setStoreName(admin.getStoreName());
        vo.setHotAvailable("AVAILABLE".equals(temperature) ? 1 : 0);

        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = java.util.UUID.randomUUID().toString() + ext;

            String filePath = "C:/upload/menuImg/";

            try {
                java.io.File dir = new java.io.File(filePath);
                if (!dir.exists()) dir.mkdirs();
                file.transferTo(new java.io.File(filePath + fileName));
                vo.setMenuImg(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MenuVO original = menuService.getMenuById(vo.getMenuId());
            vo.setMenuImg(original.getMenuImg());
        }

        menuService.updateMenu(vo);

        // 수정 완료 알림 플래그 설정
        session.setAttribute("updateSuccess", true);

        return "redirect:/admin/updateMenu/" + vo.getMenuId();
    }



}
