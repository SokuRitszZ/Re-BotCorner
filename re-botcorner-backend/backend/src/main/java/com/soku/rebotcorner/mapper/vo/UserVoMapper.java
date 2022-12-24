package com.soku.rebotcorner.mapper.vo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserVoMapper extends BaseMapper<UserVo> {

  @Select("" +
    "select id, username, head_icon " +
    "from user " +
    "where id=#{id}; "
  )
  UserVo selectById(@Param("id") Integer id);
}
