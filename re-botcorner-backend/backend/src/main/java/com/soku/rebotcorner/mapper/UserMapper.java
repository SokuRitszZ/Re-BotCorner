package com.soku.rebotcorner.mapper;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
  @Select("" +
    "select id, username, head_icon as headIcon " +
    "from user " +
    "where id=#{id}; "
  )
  JSONObject selectBaseById(@Param("id") Integer id);
}
