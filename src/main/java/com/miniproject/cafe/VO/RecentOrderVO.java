package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderVO {
    private String menuId;
    private String menuName;
    private String menuImg;
    private String temp;
    private int quantity;
    private int totalPrice;
    private Date orderTime;
    private String storeName;
}
