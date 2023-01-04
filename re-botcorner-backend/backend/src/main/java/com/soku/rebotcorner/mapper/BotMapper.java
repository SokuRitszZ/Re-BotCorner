package com.soku.rebotcorner.mapper;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.Bot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BotMapper extends BaseMapper<Bot> {
  @Select("" +
    "select id, title, user_id as userId " +
    "from bot " +
    "where id = #{id};"
  )
  JSONObject getBaseBotById(@Param("id") Integer id);

  @Select("" +
    "select id, title, description, game_id as gameId, lang_id as langId, create_time as createTime, modify_time as modifyTime, visible " +
    "from bot " +
    "where user_id = #{user_id};"
  )
  List<JSONObject> getDetailBotById(@Param("user_id") Integer userId);

  @Update("" +
    "update bot " +
    "set visible = #{visible} " +
    "where id = #{id} and user_id = #{user_id};"
  )
  void changeVisible(@Param("user_id") Integer userId, @Param("id") Integer id, @Param("visible") boolean visible);
}
