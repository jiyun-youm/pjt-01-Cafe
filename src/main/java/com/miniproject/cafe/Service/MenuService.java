package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.MenuVO;


import java.util.List;

public interface MenuService {
    List<MenuVO> getAllMenu();
    List<MenuVO> getMenuByCategory(String category);
    MenuVO getMenuById(String menuId);
    int insertMenu(MenuVO menu);
    int updateMenu(MenuVO menu);
    int deleteMenu(String menuId);
    List<MenuVO> getMenuByStoreAndCategory(String storeName, String category);

}
