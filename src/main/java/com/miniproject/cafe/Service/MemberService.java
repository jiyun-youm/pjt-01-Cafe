package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface MemberService {
    String registerMember(MemberVO vo);
//    boolean loginMember(MemberVO vo, HttpSession session);
    boolean isIdDuplicate(String id);
    boolean isEmailDuplicate(String email);

//    // ⭐ 추가: 일반 로그인 전용
//    MemberVO login(String email, String password);
//
//    // ⭐ 추가: 자동로그인(remember-me) 쿠키 저장
//    void saveRememberMeToken(String memberId, HttpServletResponse response);
}
