package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.AdminRevenueMapper;
import com.miniproject.cafe.Mapper.OrderDetailMapper;
import com.miniproject.cafe.Service.AdminRevenueService;
import com.miniproject.cafe.VO.AdminRevenueVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminRevenueServiceImpl implements AdminRevenueService {
    @Autowired
    private AdminRevenueMapper adminRevenueMapper;

    @Override
    public List<AdminRevenueVO> getAllOrders() {
        return adminRevenueMapper.getAllOrders();
    }

    @Override
    public List<AdminRevenueVO> getOrdersByDate(String date) {
        return adminRevenueMapper.getOrdersByDate(date);
    }

    @Override
    public List<AdminRevenueVO> getOrdersByRange(String startDate, String endDate) {
        return adminRevenueMapper.getOrdersByRange(startDate, endDate);
    }

}
