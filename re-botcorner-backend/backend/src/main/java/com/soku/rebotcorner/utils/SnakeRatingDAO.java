package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.SnakeRatingMapper;
import com.soku.rebotcorner.pojo.SnakeRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SnakeRatingDAO {
  @Autowired
  public static SnakeRatingMapper snakeRatingMapper;

  public static void insert(SnakeRating snakeRating) {
    snakeRatingMapper.insert(snakeRating);
  }

  public static void updateById(SnakeRating snakeRating) {
    snakeRatingMapper.updateById(snakeRating);
  }

  public static SnakeRating selectById(Integer id) {
    return snakeRatingMapper.selectById(id);
  }
}
