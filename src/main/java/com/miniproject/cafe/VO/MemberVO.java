package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String rememberToken;          // 자동로그인 토큰
    private LocalDateTime rememberExpire;  // 자동로그인 만료시간
}
