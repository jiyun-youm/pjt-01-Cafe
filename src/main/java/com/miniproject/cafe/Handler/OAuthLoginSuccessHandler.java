package com.miniproject.cafe.Handler;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberMapper memberMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        FlashMap flash = new FlashMap();
        flash.put("loginSuccessType", "oauth");
        flash.put("loginMemberName", member.getUsername());
        new SessionFlashMapManager().saveOutputFlashMap(flash, request, response);

        response.sendRedirect("/home/");
    }
}