package com.soku.rebotcorner.mapper;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
  @Select("" +
    "select id, username, avatar " +
    "from user " +
    "where id=#{id}; "
  )
  JSONObject getBaseById(@Param("id") Integer id);

  @Select("" +
    "select id, username, avatar, signature " +
    "from user " +
    "where id=#{id}; "
  )
  JSONObject getProfileById(@Param("id") Integer id);
}
