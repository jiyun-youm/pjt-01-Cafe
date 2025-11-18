package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserLikeMapper {

    // 찜 추가
    int insertLike(@Param("userId") String userId, @Param("menuId") String menuId);

    // 찜 삭제
    int deleteLike(@Param("userId") String userId, @Param("menuId") String menuId);

    // 로그인한 유저의 찜 메뉴 id만 조회 (사용 여부 선택)
    List<String> getLikedMenuIds(String userId);

    // 찜 여부 확인
    int isLiked(@Param("userId") String userId, @Param("menuId") String menuId);

    // 찜한 메뉴 상세 리스트 조회
    List<MenuVO> getLikedMenus(String userId);
}
