package com.soku.rebotcorner.mapper.vo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.vo.RatingVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RatingVoMapper extends BaseMapper<RatingVo> {
  @Select(
    "select u.id as id, u.head_icon as head_icon, u.username as username, sr.rating as rating\n" +
    "from user u, snake_rating sr\n" +
    "where u.id = sr.id\n" +
    "order by sr.rating desc\n" +
    "limit 0,10;"
  )
  List<RatingVo> getSnakeRatings();

  @Select(
    "select u.id as id, u.head_icon as head_icon, u.username as username, sr.rating as rating\n" +
    "from user u, reversi_rating sr\n" +
    "where u.id = sr.id\n" +
    "order by sr.rating desc\n" +
    "limit 0,10;"
  )
  List<RatingVo> getReversiRating();

  @Select(
    "select u.id as id, u.head_icon as head_icon, u.username as username, sr.rating as rating\n" +
      "from user u, backgammon_rating sr\n" +
      "where u.id = sr.id\n" +
      "order by sr.rating desc\n" +
      "limit 0,10;"
  )
  List<RatingVo> getBackgammonRating();
}
