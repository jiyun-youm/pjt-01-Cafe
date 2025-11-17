package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    List<MenuVO> getAllMenu();
    List<MenuVO> getMenuByCategory(String category);
    MenuVO getMenuById(String menuId);
    int insertMenu(MenuVO menu);
    int updateMenu(MenuVO menu);
    int deleteMenu(String menuId);
    List<MenuVO> getMenuByStoreAndCategory(
            @Param("storeName") String storeName,
            @Param("category") String category
    );
}
