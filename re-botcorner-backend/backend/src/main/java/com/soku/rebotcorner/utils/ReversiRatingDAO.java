package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.ReversiRatingMapper;
import com.soku.rebotcorner.pojo.ReversiRating;
import org.springframework.beans.factory.annotation.Autowired;

public class ReversiRatingDAO {
  @Autowired
  public static ReversiRatingMapper reversiRatingMapper;

  public static void insert(ReversiRating reversiRating) {
    reversiRatingMapper.insert(reversiRating);
  }

  public static ReversiRating selectById(Integer id) {
    return reversiRatingMapper.selectById(id);
  }

  public static void updateById(ReversiRating reversiRating) {
    reversiRatingMapper.updateById(reversiRating);
  }
}
