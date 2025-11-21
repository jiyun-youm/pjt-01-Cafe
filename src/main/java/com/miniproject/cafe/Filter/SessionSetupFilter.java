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
    private final AdminMapper adminMapper; // [추가] 관리자 매퍼

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 현재 스프링 시큐리티 상에서 인증된 사용자인지 확인 (Remember-Me로 복구된 상태 포함)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            HttpSession session = request.getSession();
            String principalName = auth.getName(); // ID 또는 Email

            // 2. 권한 확인 (관리자 vs 일반 회원)
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                // === [관리자] 세션 복구 ===
                if (session.getAttribute("admin") == null) {
                    // AdminMapper에 아이디로 조회하는 메서드(findById)가 있다고 가정
                    AdminVO admin = adminMapper.findById(principalName);
                    if (admin != null) {
                        session.setAttribute("admin", admin);
                    }
                }
            } else {
                // === [일반 회원] 세션 복구 ===
                if (session.getAttribute("member") == null) {
                    MemberVO member = memberMapper.findByEmail(principalName);
                    if (member != null) {
                        session.setAttribute("member", member);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}