package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.MenuMapper;
import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;


    @Override
    public List<MenuVO> getMenuByStoreAndCategory(String storeName, String category) {
        return menuMapper.getMenuByStoreAndCategory(storeName, category);
    }

    @Override
    public List<MenuVO> getMenuByStore(String storeName) {
        return menuMapper.getMenuByStore(storeName);
    }

    @Override
    public void insertMenu(MenuVO menuVO) {
        menuMapper.insertMenu(menuVO);
    }

    @Override
    public void deleteMenuByStore(String menuId, String storeName) {
        menuMapper.deleteMenuByStore(menuId, storeName);
    }

    @Override
    public String getLastMenuIdByStore(String storeName) {
        return menuMapper.getLastMenuIdByStore(storeName);
    }
}
