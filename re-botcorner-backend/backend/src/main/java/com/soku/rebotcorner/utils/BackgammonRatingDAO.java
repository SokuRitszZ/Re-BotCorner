package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.BackgammonRatingMapper;
import com.soku.rebotcorner.pojo.BackgammonRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BackgammonRatingDAO {
  @Autowired
  public static BackgammonRatingMapper backgammonRatingMapper;

  public static void insert(BackgammonRating backgammonRating) {
    backgammonRatingMapper.insert(backgammonRating);
  }

  public static void updateById(BackgammonRating backgammonRating) {
    backgammonRatingMapper.updateById(backgammonRating);
  }

  public static BackgammonRating selectById(Integer id) {
    return backgammonRatingMapper.selectById(id);
  }
}
