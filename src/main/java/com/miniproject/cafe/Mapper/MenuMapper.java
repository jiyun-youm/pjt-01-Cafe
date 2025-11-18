package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {
    List<MenuVO> getMenuByStoreAndCategory(
            @Param("storeName") String storeName,
            @Param("category") String category
    );
    List<MenuVO> getMenuByStore(String storeName);
    void insertMenu(MenuVO menuVO);
    void deleteMenuByStore(String menuId, String storeName);
    String getLastMenuIdByStore(String storeName);

}
