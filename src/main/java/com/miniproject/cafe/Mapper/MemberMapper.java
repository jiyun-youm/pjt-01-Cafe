package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface MemberMapper {

    void registerMember(MemberVO vo);
    boolean isIdDuplicate(String id);
    boolean isEmailDuplicate(String email);

    MemberVO findByEmail(String email);

    void insertOAuthMember(MemberVO vo);

    void updateUser(MemberVO vo);
}
