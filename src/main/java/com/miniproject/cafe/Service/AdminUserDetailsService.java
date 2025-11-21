package com.miniproject.cafe.Service;

import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.VO.AdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminVO admin = adminMapper.findById(username);

        if (admin == null) {
            throw new UsernameNotFoundException("Admin not found: " + username);
        }

        return User.withUsername(admin.getId())
                .password(admin.getPassword())
                .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }
}