package com.miniproject.cafe.Service;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberVO member = memberMapper.findByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        return User.withUsername(member.getEmail())
                .password(member.getPassword())
                .roles("USER")
                .build();
    }
}