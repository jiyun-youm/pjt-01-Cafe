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
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String msg = exception.getMessage();
        if (msg == null) msg = "";

        String alertMsg;

        // [핵심] Service에서 보낸 "provider=" 힌트 확인
        if (msg.contains("provider=")) {
            // "provider=" 뒤에 있는 단어(naver, kakao 등) 추출
            String provider = msg.substring(msg.indexOf("provider=") + 9).trim();

            // [해결] 문장 끝에 점(.) 제거
            alertMsg = switch (provider) {
                case "naver" -> "이미 네이버 계정이 존재합니다. 네이버로 로그인해주세요";
                case "kakao" -> "이미 카카오 계정이 존재합니다. 카카오로 로그인해주세요";
                case "google" -> "이미 구글 계정이 존재합니다. 구글로 로그인해주세요";
                default -> "이미 " + provider + " 계정으로 가입되어 있습니다";
            };
        } else {
            alertMsg = "소셜 로그인 중 오류가 발생했습니다";
        }

        // 루트(/)로 리다이렉트 (ContextPath 포함)
        String redirectUrl = request.getContextPath() + "/home/?oauthError=" +
                URLEncoder.encode(alertMsg, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}