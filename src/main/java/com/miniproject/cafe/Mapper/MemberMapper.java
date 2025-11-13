package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface MemberMapper {
    void registerMember(MemberVO vo); //성공 시 "SUCCESS", 아이디 중복 시 "ID_DUPLICATE", 이메일 중복 시 "EMAIL_DUPLICATE"
    MemberVO loginMember(MemberVO vo); //ID와 Password가 일치하는 회원을 조회
    boolean isIdDuplicate(String id); //id 중복확인 용도
    boolean isEmailDuplicate(String email); //이메일 중복확인 용도
    Optional<MemberVO> findByEmail(@Param("email") String email); //이메일과 로그인 방식(카카오톡,네이버 등)
    void insertOAuthMember(MemberVO vo); //OAuth 사용자를 위한 회원가입
    void updateUser(MemberVO vo); //사용자 정보 업데이트
}