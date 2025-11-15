package com.miniproject.cafe.Handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
public class FormLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        FlashMap flash = new FlashMap();
        flash.put("loginErrorMessage", "이메일 또는 비밀번호가 잘못되었습니다.");

        new SessionFlashMapManager().saveOutputFlashMap(flash, request, response);

        response.sendRedirect("/home/");
    }
}