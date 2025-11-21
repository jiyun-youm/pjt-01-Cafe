package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.RewardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RewardMapper {
    RewardVO findByMemberId(@Param("memberId") String memberId);
    void insertReward(RewardVO reward);
    void updateReward(RewardVO reward);
}
