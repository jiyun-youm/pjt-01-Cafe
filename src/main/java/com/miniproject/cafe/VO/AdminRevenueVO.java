package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRevenueVO {
    private String viewDate;
    private String orderTime;
    private int orderId;
    private int orderAmount;
    private String orderType;
}
