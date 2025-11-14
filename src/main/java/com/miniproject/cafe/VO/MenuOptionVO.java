package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuOptionVO {
    private long optionId; // 옵션 ID (PK)
    private String menuId; // 메뉴 ID (FK)
    private String temp; // 메뉴 온도
    private boolean tumblerUse; // 텀블러 사용 여부
    private int shotCount; // 샷 추가 횟수
    private int vanillaSyrupCount; // 바닐라 시럽 추가 횟수
    private int whippedCreamCount; // 휘핑크림 추가 횟수
}
