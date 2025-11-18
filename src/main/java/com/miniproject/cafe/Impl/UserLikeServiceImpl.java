package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.UserLikeMapper;
import com.miniproject.cafe.Service.UserLikeService;
import com.miniproject.cafe.VO.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLikeServiceImpl implements UserLikeService {

    private final UserLikeMapper userLikeMapper;

    @Override
    public boolean toggleLike(String userId, String menuId) {

        System.out.println("toggleLike 실행 userId = " + userId + ", menuId = " + menuId);

        boolean alreadyLiked = userLikeMapper.isLiked(userId, menuId) > 0;

        if (alreadyLiked) {
            userLikeMapper.deleteLike(userId, menuId);
            System.out.println("찜 삭제 완료");
            return false;
        } else {
            userLikeMapper.insertLike(userId, menuId);
            System.out.println("찜 등록 완료");
            return true;
        }
    }

    @Override
    public List<MenuVO> getLikedMenus(String userId) {
        System.out.println("찜 리스트 조회 userId = " + userId);
        return userLikeMapper.getLikedMenus(userId);
    }

    @Override
    public boolean isLiked(String userId, String menuId) {
        return userLikeMapper.isLiked(userId, menuId) > 0;
    }
}
