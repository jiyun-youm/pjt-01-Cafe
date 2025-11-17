package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.Service.AdminService;
import com.miniproject.cafe.VO.AdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(AdminVO vo) {

        if (mapper.checkId(vo.getId()) > 0) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        mapper.insertAdmin(vo);
    }

    @Override
    public AdminVO login(String id, String password) {

        AdminVO vo = new AdminVO();
        vo.setId(id);

        AdminVO dbVO = mapper.loginAdmin(vo);
        if (dbVO == null) {
            throw new RuntimeException("아이디가 존재하지 않습니다.");
        }

        if (!passwordEncoder.matches(password, dbVO.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return dbVO;
    }

    @Override
    public int checkId(String id) {
        return mapper.checkId(id);
    }

}
