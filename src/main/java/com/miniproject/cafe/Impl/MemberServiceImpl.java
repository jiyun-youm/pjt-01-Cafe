package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Service.MemberService;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String registerMember(MemberVO vo) {
        if (vo.getPassword() == null || vo.getPasswordCheck() == null || !vo.getPassword().equals(vo.getPasswordCheck())) {
            return "PASSWORD_MISMATCH";
        }

        if (memberMapper.isIdDuplicate(vo.getId())) {
            return "ID_DUPLICATE";
        }

        if (memberMapper.isEmailDuplicate(vo.getEmail())) {
            return "EMAIL_DUPLICATE";
        }
        String rawPassword = vo.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        vo.setPassword(encodedPassword);
        vo.setProvider("general");

        memberMapper.registerMember(vo);
        return "SUCCESS";
    }

    @Override
    public boolean loginMember(MemberVO vo, HttpSession session) {
        MemberVO storedMember = memberMapper.loginMember(vo);

        if (storedMember != null) {
            // 사용자가 입력한 평문 비밀번호와 DB에 저장된 암호화된 비밀번호를 비교
            if (passwordEncoder.matches(vo.getPassword(), storedMember.getPassword())) {
                session.setAttribute("loginMember", storedMember); // 비밀번호 일치 (로그인 성공)
                return true;
            }
        }
        return false; //비밀번호 불일치 또는 회원 없음
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