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
    public List<MenuVO> getAllMenu() {
        return menuMapper.getAllMenu();
    }

    @Override
    public MenuVO getMenuById(String menuId) {
        return menuMapper.getMenuById(menuId);
    }

    @Override
    public List<MenuVO> getMenuByCategory(String category) {
        return menuMapper.getMenuByCategory(category);
    }

    @Override
    public int insertMenu(MenuVO menu) {
        return menuMapper.insertMenu(menu);
    }

    @Override
    public int updateMenu(MenuVO menu) {
        return menuMapper.updateMenu(menu);
    }

    @Override
    public int deleteMenu(String menuId) {
        return menuMapper.deleteMenu(menuId);
    }

    @Override
    public List<MenuVO> getMenuByRegionAndCategory(String region, String category) {
        return menuMapper.getMenuByRegionAndCategory(region, category);
    }
}
