package com.soku.rebotcorner.mapper;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.Bot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BotMapper extends BaseMapper<Bot> {
  @Select("" +
    "select id, title, user_id as userId " +
    "from bot " +
    "where id = #{id};"
  )
  JSONObject getBaseBotById(@Param("id") Integer id);
}
