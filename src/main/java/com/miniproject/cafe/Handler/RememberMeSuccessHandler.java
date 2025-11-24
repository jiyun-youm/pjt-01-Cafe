package com.miniproject.cafe.Handler;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class RememberMeSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberMapper memberMapper;

    @Autowired
    public RememberMeSuccessHandler(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {

        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        if (member != null) {
            request.getSession().setAttribute("LOGIN_USER_ID", member.getId());
        }
    }
}