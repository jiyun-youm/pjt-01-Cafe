package com.miniproject.cafe.Handler;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberMapper memberMapper;
    private final RememberMeServices rememberMeServices;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        rememberMeServices.loginSuccess(request, response, authentication);

        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        if (member == null) {
            response.sendRedirect("/home/login?oauth_error=user_not_found");
            return;
        }

        request.getSession().setAttribute("member", member);

        String encodedName = URLEncoder.encode(member.getUsername(), StandardCharsets.UTF_8);

        response.sendRedirect("/home/?oauthSuccess=true&username=" + encodedName);
    }
}
