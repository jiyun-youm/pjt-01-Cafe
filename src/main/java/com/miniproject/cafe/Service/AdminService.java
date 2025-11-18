package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.AdminVO;

public interface AdminService {

    void register(AdminVO vo);          // 회원가입
    AdminVO login(AdminVO vo);  // 로그인

    int checkId(String id);
}