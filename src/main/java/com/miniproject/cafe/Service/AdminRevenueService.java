package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.AdminRevenueVO;
import com.miniproject.cafe.VO.MenuVO;

import java.util.List;

public interface AdminRevenueService {
    List<AdminRevenueVO> getOrdersByDate(String date);
    List<AdminRevenueVO> getAllOrders();
    List<AdminRevenueVO> getOrdersByRange(String startDate, String endDate);

}
