package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberVO {
    private String id;
    private String password;
    private String username;
    private String email;
    private String passwordCheck; //비밀번호 추가 확인 로직
    private String provider; //sns를 통한 회원가입
}
