package com.soku.rebotcorner.mapper;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RatingMapper extends BaseMapper<Rating> {
  @Select("" +
    "select u.id as id, u.head_icon as headIcon, u.username as username, rt.score as score\n" +
    "from user u, rating rt\n" +
    "where u.id = rt.user_id and game_id = #{game_id}\n" +
    "order by rt.score desc\n" +
    "limit 0,10;"
  )
  List<JSONObject> getTop10(@Param("game_id") Integer gameId);

  @Update("" +
    "update rating set score = #{score} " +
    "where user_id = #{user_id} and game_id = #{game_id} ")
  void update(@Param("user_id") Integer userId, @Param("game_id") Integer gameId, @Param("score") Integer score);
}
