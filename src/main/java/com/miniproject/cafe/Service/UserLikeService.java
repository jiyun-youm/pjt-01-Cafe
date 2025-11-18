package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.MenuVO;

import java.util.List;

public interface UserLikeService {
    boolean toggleLike(String userId, String menuId);
    List<MenuVO> getLikedMenus(String userId);
    boolean isLiked(String userId, String menuId);
}
