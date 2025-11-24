package com.miniproject.cafe.Filter;

import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.AdminVO;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class SessionSetupFilter extends OncePerRequestFilter {

    private final MemberMapper memberMapper;
    private final AdminMapper adminMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            HttpSession session = request.getSession();
            Object principal = auth.getPrincipal();

            // 일반 회원
            if (principal instanceof MemberVO member) {
                if (session.getAttribute("member") == null) {
                    session.setAttribute("member", member);
                    session.setAttribute("LOGIN_USER_ID", member.getId());
                }
            }
            // 관리자
            else if (principal instanceof AdminVO admin) {
                if (session.getAttribute("admin") == null) {
                    session.setAttribute("admin", admin);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}