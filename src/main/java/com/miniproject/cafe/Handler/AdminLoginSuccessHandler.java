package com.miniproject.cafe.Handler;

import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.VO.AdminVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor // 생성자 자동 생성
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AdminMapper adminMapper;
    private final RememberMeServices rememberMeServices;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        // remember-me 쿠키 발급
        rememberMeServices.loginSuccess(request, response, authentication);

        // 로그인한 관리자 정보 조회
        String adminId = authentication.getName();
        AdminVO admin = adminMapper.findById(adminId);

        HttpSession session = request.getSession();
        if (admin != null) {
            session.setAttribute("admin", admin);
            session.setAttribute("ADMIN_ID", admin.getId());
        }

        try {
            response.sendRedirect("/admin/orders");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}