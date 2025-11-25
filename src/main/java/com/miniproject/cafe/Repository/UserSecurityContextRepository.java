package com.miniproject.cafe.Repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

public class UserSecurityContextRepository implements SecurityContextRepository {

    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT_USER";

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        HttpSession session = request.getSession(false);

        SecurityContext context = null;
        if (session != null) {
            Object ctx = session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
            if (ctx instanceof SecurityContext) {
                context = (SecurityContext) ctx;
            }
        }

        if (context == null) {
            context = SecurityContextHolder.createEmptyContext();
        }
        return context;
    }

    @Override
    public void saveContext(SecurityContext context,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        HttpSession session = request.getSession(false);

        // 인증 정보가 없으면 세션에서 사용자 컨텍스트만 제거
        if (context == null || context.getAuthentication() == null) {
            if (session != null) {
                session.removeAttribute(SPRING_SECURITY_CONTEXT_KEY);
            }
            return;
        }

        // 세션이 없으면 새로 생성
        if (session == null) {
            session = request.getSession(true);
        }

        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        return session.getAttribute(SPRING_SECURITY_CONTEXT_KEY) != null;
    }
}