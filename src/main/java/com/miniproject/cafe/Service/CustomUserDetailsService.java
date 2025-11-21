package com.miniproject.cafe.Service;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1) 기본값은 그대로 사용
        String email = username;

        // 2) OAuth2 + remember-me 에서
        //    "Name: [이메일], Granted Authorities: ... User Attributes: ..." 형태로 넘어오는 경우 방어
        if (username != null
                && username.startsWith("Name: [")
                && username.contains("], Granted Authorities")) {

            int start = username.indexOf("Name: [") + "Name: [".length();
            int end = username.indexOf("], Granted Authorities");

            if (start >= 0 && end > start) {
                email = username.substring(start, end).trim();
            }
        }

        // 3) 이제 정제된 email로 DB 조회
        MemberVO member = memberMapper.findByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        // 4) Spring Security 기본 UserDetails 생성
        return User.withUsername(member.getEmail())
                .password(member.getPassword())
                .roles("USER")
                .build();
    }
}
