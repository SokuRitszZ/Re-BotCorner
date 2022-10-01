package com.soku.rebotcorner.service.impl.getrating;

import com.soku.rebotcorner.mapper.BackgammonRatingMapper;
import com.soku.rebotcorner.pojo.BackgammonRating;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.getrating.BackgammonRatingService;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackgammonRatingServiceImpl implements BackgammonRatingService {
  @Autowired
  private BackgammonRatingMapper backgammonRatingMapper;

  @Override
  public List<Map<String, String>> getRatingList() {
    List<BackgammonRating> backgammonRatingList = backgammonRatingMapper.selectList(null);
    List<Map<String, String>> result = new ArrayList<>();
    for (BackgammonRating backgammonRating : backgammonRatingList) {
      Map<String, String> one = new HashMap<>();
      Integer userId = backgammonRating.getId();
      User user = UserDAO.selectById(userId);
      one.put("userId", userId.toString());
      one.put("rating", backgammonRating.getRating().toString());
      one.put("headIcon", user.getHeadIcon());
      one.put("username", user.getUsername());
      result.add(one);
    }
    return result;
  }
}
