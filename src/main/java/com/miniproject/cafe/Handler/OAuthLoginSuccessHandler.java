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

        // 여기서 getName() 은 CustomOAuth2UserService 에서 principal name 으로 사용한 "email" (가상 이메일)
        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        // 자동가입 실패 등의 예외 상황 방어
        if (member == null) {
            response.sendRedirect("/home/login?oauth_error=user_not_found");
            return;
        }

        // 세션에 로그인 회원 저장
        request.getSession().setAttribute("member", member);

        // 로그인 성공 토스트 메시지용 flash
        FlashMap flash = new FlashMap();
        flash.put("loginSuccessType", "oauth");
        flash.put("loginMemberName", member.getUsername());
        new SessionFlashMapManager().saveOutputFlashMap(flash, request, response);

        // 홈으로 이동
        response.sendRedirect("/home/");
    }
}
