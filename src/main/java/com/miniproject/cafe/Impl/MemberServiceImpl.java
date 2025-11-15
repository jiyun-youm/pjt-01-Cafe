package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Service.MemberService;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String registerMember(MemberVO vo) {

        if (!vo.getPassword().equals(vo.getPasswordCheck())) {
            return "PASSWORD_MISMATCH";
        }

        if (memberMapper.isIdDuplicate(vo.getId())) {
            return "ID_DUPLICATE";
        }

        if (memberMapper.isEmailDuplicate(vo.getEmail())) {
            return "EMAIL_DUPLICATE";
        }

        vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        vo.setProvider("general");

        memberMapper.registerMember(vo);
        return "SUCCESS";
    }

    @Override
    public boolean isIdDuplicate(String id) {
        return memberMapper.isIdDuplicate(id);
    }

    @Override
    public boolean isEmailDuplicate(String email) {
        return memberMapper.isEmailDuplicate(email);
    }
}