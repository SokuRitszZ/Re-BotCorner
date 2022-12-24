package com.soku.rebotcorner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.BackgammonRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BackgammonRatingMapper extends BaseMapper<BackgammonRating> {
  @Select(
    "select u.id as id, u.head_icon as headIcon, u.username as username, sr.rating as rating\n" +
      "from user u, backgammon_rating sr\n" +
      "where u.id = sr.id\n" +
      "order by sr.rating desc\n" +
      "limit 0,10;"
  )
  List<Map<String, Object>> getTop10();
}
