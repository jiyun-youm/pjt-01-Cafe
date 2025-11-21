package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.MemberVO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final MemberVO memberVO;
    private final Map<String, Object> attributes;

    // 일반 로그인 생성자
    public CustomUserDetails(MemberVO memberVO) {
        this.memberVO = memberVO;
        this.attributes = null;
    }

    // OAuth2 로그인 생성자
    public CustomUserDetails(MemberVO memberVO, Map<String, Object> attributes) {
        this.memberVO = memberVO;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return memberVO.getPassword();
    }

    @Override
    public String getUsername() {
        return memberVO.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User 필수 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return memberVO.getUsername();
    }
}
