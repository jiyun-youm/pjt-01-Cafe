package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.AdminRevenueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminRevenueMapper {
    List<AdminRevenueVO> getAllOrders();
    List<AdminRevenueVO> getOrdersByDate(String date);
    List<AdminRevenueVO> getOrdersByRange(@Param("startDate") String startDate, @Param("endDate") String endDate);


}
