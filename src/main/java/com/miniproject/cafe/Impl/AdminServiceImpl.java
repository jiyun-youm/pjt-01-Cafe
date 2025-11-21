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

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(AdminVO vo) {

        if (adminMapper.checkId(vo.getId()) > 0) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        adminMapper.insertAdmin(vo);
    }

    @Override
    public AdminVO login(AdminVO adminVO) {

        AdminVO dbVO = adminMapper.loginAdmin(adminVO);

        if (dbVO == null) {
            return null;
        }

        if (!passwordEncoder.matches(adminVO.getPassword(), dbVO.getPassword())) {
            return null;
        }

        return dbVO;
    }

    @Override
    public int checkId(String id) {
        return adminMapper.checkId(id);
    }

    @Override
    public AdminVO findById(String id) {
        return adminMapper.findById(id);
    }

}
