package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.OrderItemVO;
import com.miniproject.cafe.VO.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    List<OrderVO> findOrdersByStore(@Param("storeName") String storeName);
    void insertOrder(OrderVO order); //주문조회
    void insertOrderDetails(List<OrderItemVO> list);
    void updateOrderStatus(@Param("status") String status,
                           @Param("orderId") Long orderId); //주문 상태 완료 취소
}
