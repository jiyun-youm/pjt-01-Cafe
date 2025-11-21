package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.MenuVO;


import java.util.List;

public interface MenuService {
    List<MenuVO> getMenuByStoreAndCategory(String storeName, String category);
    List<MenuVO> getMenuByStore(String storeName);
    MenuVO getMenuById(String menuId);
    void insertMenu(MenuVO menuVO);
    void updateMenu(MenuVO menuVO);
    void deleteMenuByStore(String menuId, String storeName);
    String getLastMenuIdByStore(String storeName);
    void updateSalesStatus(String menuId, String storeName, String saleStatus);
}
