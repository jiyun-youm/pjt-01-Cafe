package com.miniproject.cafe.Service;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.OrderVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface MemberService {

    List<OrderVO> getAdminOrderList();

    OrderVO getOrderDetail(String orderId);
}
