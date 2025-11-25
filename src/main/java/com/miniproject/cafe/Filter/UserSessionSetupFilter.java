package com.miniproject.cafe.Filter;

import com.miniproject.cafe.Mapper.*;
import com.miniproject.cafe.Service.CustomUserDetails;
import com.miniproject.cafe.VO.CouponVO;
import com.miniproject.cafe.VO.MemberVO;
import com.miniproject.cafe.VO.RecentOrderVO;
import com.miniproject.cafe.VO.RewardVO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class UserSessionSetupFilter extends OncePerRequestFilter {

    private final MemberMapper memberMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (response.isCommitted()) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            HttpSession session = request.getSession(true);
            Object principal = auth.getPrincipal();
            String loginId = auth.getName();

            if(session.getAttribute("member") == null) {
                MemberVO member = null;

                if(principal instanceof MemberVO) {
                    member = (MemberVO) principal;
                } else if (principal instanceof CustomUserDetails) {
                    member = ((CustomUserDetails) principal).getMemberVO();
                } else if (principal instanceof UserDetails) {
                    member = memberMapper.findByEmail(((UserDetails) principal).getUsername());
                } else {
                    member = memberMapper.findByEmail(loginId);
                }

                if(member != null) {
                    session.setAttribute("member", member);
                    session.setAttribute("LOGIN_USER_ID", member.getId());

                    System.out.println("✅ [UserFilter] 사용자 세션 복구 완료: " + member.getEmail());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}