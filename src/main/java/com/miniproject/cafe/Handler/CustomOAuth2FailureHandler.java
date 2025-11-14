package com.miniproject.cafe.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "로그인 중 오류가 발생했습니다.";

        if (exception instanceof OAuth2AuthenticationException) {
            errorMessage = exception.getMessage();
        }

        // --- [ 여기가 수정됩니다 ] ---

        // 1. URL 인코딩 대신, request에서 세션을 가져옵니다.
        HttpSession session = request.getSession();

        // 2. 세션에 1회성 에러 메시지를 저장합니다.
        session.setAttribute("oauthErrorMessage", errorMessage);

        // 3. 쿼리 파라미터 없이 깔끔한 /home/으로 리디렉션합니다.
        response.sendRedirect("/home/");
    }
}
