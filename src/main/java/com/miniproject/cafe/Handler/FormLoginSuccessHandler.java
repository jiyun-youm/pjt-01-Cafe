package com.miniproject.cafe.Handler;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;

@RequiredArgsConstructor
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberMapper memberMapper;
    private final RememberMeServices rememberMeServices;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        // remember-me 쿠키 발행
        rememberMeServices.loginSuccess(request, response, authentication);

        // 사용자 정보 조회
        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        HttpSession session = request.getSession();

        if (member != null) {
            session.setAttribute("member", member);
            session.setAttribute("LOGIN_USER_ID", member.getId());
        }

        // 로그인 성공 후 redirect + 파라미터 전달
        try {
            String username = (member != null && member.getUsername() != null)
                    ? member.getUsername()
                    : email;

            response.sendRedirect("/home/?loginSuccess=true&username=" +
                    java.net.URLEncoder.encode(username, "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
