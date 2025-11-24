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
    public boolean toggleLike(@RequestParam("menuId") String menuId, Authentication auth, HttpSession session) {

        String sessionUserId = (String) session.getAttribute("LOGIN_USER_ID");

        System.out.println("### 로그인 사용자 확인: auth = " + (auth != null ? auth.getName() : "NULL") + ", session.LOGIN_USER_ID = " + sessionUserId);
        if (auth == null) {
            System.out.println("### 회원 세션이 없어서 찜 불가");
            return false;
        }

        String userId = sessionUserId;
        System.out.println("### 최종 사용 ID = " + userId);

        return userLikeService.toggleLike(userId, menuId);
    }

    @GetMapping("/list")
    public List<MenuVO> getLikedMenus(Authentication auth, HttpSession session) {

        String sessionUserId = (String) session.getAttribute("LOGIN_USER_ID");
        if (sessionUserId == null) {
            return null;
        }
        return userLikeService.getLikedMenus(sessionUserId);
    }
}
