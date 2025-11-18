package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.UserLikeService;
import com.miniproject.cafe.VO.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class UserLikeController {

    private final UserLikeService userLikeService;

    // 찜 토글
    @PostMapping("/toggle")
    public boolean toggleLike(@RequestParam("menuId") String menuId, Authentication auth) {

        System.out.println("### 로그인 사용자 확인: " + (auth != null ? auth.getName() : "NULL"));
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String userId = auth.getName(); // 로그인 ID 가져오기
        System.out.println("### 인증된 userId = " + userId);
        return userLikeService.toggleLike(userId, menuId);
    }

    @GetMapping("/list")
    public List<MenuVO> getLikedMenus(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String userId = auth.getName();
        return userLikeService.getLikedMenus(userId);
    }


}
