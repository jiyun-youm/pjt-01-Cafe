package com.miniproject.cafe.Controller;

import com.miniproject.cafe.VO.MenuVO;
import com.miniproject.cafe.Service.UserLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final UserLikeService userLikeService;

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        boolean isLoggedIn = (auth != null && auth.isAuthenticated());
        model.addAttribute("IS_LOGGED_IN", isLoggedIn);
        return "main";
    }

    @GetMapping("/order_history")
    public String order_history() {
        return "order_history";
    }

    @GetMapping("/coffee")
    public String food() {
        return "redirect:/menu/coffee";
    }

    @GetMapping("/mypick")
    public String myPickPage(Model model, Authentication auth) {

        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = auth.getName();
        List<MenuVO> likedMenus = userLikeService.getLikedMenus(userId);

        model.addAttribute("likedMenus", likedMenus);

        return "mypick";
    }

    @PostMapping("/saveRegion")
    @ResponseBody
    public String saveRegion(@RequestBody Map<String, String> data, HttpSession session) {
        String storeName = data.get("region");

        if (storeName == null || storeName.equals("selecting")) {
            session.removeAttribute("storeName");
            return "cleared";
        }

        session.setAttribute("storeName", storeName);
        return "saved";
    }

    @GetMapping("/getRegion")
    @ResponseBody
    public String getRegion(HttpSession session) {

        Object storeName = session.getAttribute("storeName");

        return storeName != null ? storeName.toString() : null;
    }
}
