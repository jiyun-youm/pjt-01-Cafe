package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.AdminVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    void insertAdmin(AdminVO vo);
    AdminVO loginAdmin(AdminVO vo);
    int checkId(String id);
    AdminVO findById(String id);
}
