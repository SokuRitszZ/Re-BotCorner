package com.soku.rebotcorner.service.impl.getrating;

import com.soku.rebotcorner.mapper.SnakeRatingMapper;
import com.soku.rebotcorner.pojo.SnakeRating;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.getrating.SnakeRatingService;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SnakeRatingServiceImpl implements SnakeRatingService {
  @Autowired
  private SnakeRatingMapper snakeRatingMapper;

  @Override
  public List<Map<String, String>> getRatingList() {
    List<SnakeRating> snakeRatingList = snakeRatingMapper.selectList(null);
    List<Map<String, String>> result = new ArrayList<>();
    for (SnakeRating snakeRating : snakeRatingList) {
      Map<String, String> one = new HashMap<>();
      Integer userId = snakeRating.getId();
      User user = UserDAO.selectById(userId);
      one.put("userId", userId.toString());
      one.put("rating", snakeRating.getRating().toString());
      one.put("headIcon", user.getHeadIcon());
      one.put("username", user.getUsername());
      result.add(one);
    }
    return result;
  }
}
