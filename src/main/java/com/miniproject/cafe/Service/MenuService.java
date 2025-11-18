package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.MenuVO;


import java.util.List;

public interface MenuService {
    List<MenuVO> getMenuByStoreAndCategory(String storeName, String category);
    List<MenuVO> getMenuByStore(String storeName);
    void insertMenu(MenuVO menuVO);
    void deleteMenuByStore(String menuId, String storeName);
    String getLastMenuIdByStore(String storeName);
}
