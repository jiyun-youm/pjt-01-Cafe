package com.miniproject.cafe.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberMapper memberMapper;
    private final RememberMeServices rememberMeServices;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환기

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1. Remember-Me 쿠키 생성 (로그인 유지 체크 시)
        rememberMeServices.loginSuccess(request, response, authentication);

        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        // 2. 세션에 멤버 정보 저장
        if(member != null) {
            request.getSession().setAttribute("member", member);
        }

        // 3. [중요] 리다이렉트 대신 JSON 응답 반환
        // 클라이언트(JS)가 이 JSON을 받아서 토스트를 띄우고 페이지를 이동시킵니다.
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> data = new HashMap<>();
        data.put("message", "로그인 성공");
        // 사용자 이름을 담아서 보내면 JS가 "xxx님 환영합니다"를 띄울 수 있습니다.
        data.put("username", member != null ? member.getUsername() : "고객");

        // JSON으로 변환하여 응답 본문에 작성
        objectMapper.writeValue(response.getWriter(), data);
    }
}