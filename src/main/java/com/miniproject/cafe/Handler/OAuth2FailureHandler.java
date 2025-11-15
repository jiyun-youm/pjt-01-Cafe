package com.miniproject.cafe.Handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        String msg = exception.getMessage();
        if (msg == null) msg = "";

        String alertMsg;

        if (msg.startsWith("provider=")) {

            String provider = msg.replace("provider=", "").trim();

            alertMsg = switch (provider) {
                case "naver" -> "이미 네이버로 가입된 이메일입니다. 네이버로 로그인해주세요.";
                case "kakao" -> "이미 카카오로 가입된 이메일입니다. 카카오로 로그인해주세요.";
                case "google" -> "이미 구글로 가입된 이메일입니다. 구글로 로그인해주세요.";
                case "general" -> "이미 일반 회원으로 가입한 이메일입니다. 이메일/비밀번호로 로그인해주세요.";
                default -> "이미 해당 이메일로 가입된 계정이 존재합니다.";
            };

        } else {
            alertMsg = "소셜 로그인 중 오류가 발생했습니다.";
        }

        response.sendRedirect("/home/?oauthError=" +
                URLEncoder.encode(alertMsg, StandardCharsets.UTF_8));
    }
}
