package com.soku.rebotcorner.service.impl.getrating;

import com.soku.rebotcorner.mapper.ReversiRatingMapper;
import com.soku.rebotcorner.pojo.ReversiRating;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.getrating.ReversiRatingService;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReversiRatingServiceImpl implements ReversiRatingService {
  @Autowired
  private ReversiRatingMapper reversiRatingMapper;

  @Override
  public List<Map<String, String>> getRatingList() {
    List<ReversiRating> reversiRatings = reversiRatingMapper.selectList(null);
    List<Map<String, String>> result = new ArrayList<>();
    for (ReversiRating reversiRating : reversiRatings) {
      Map<String, String> one = new HashMap<>();
      Integer userId = reversiRating.getId();
      User user = UserDAO.selectById(userId);
      one.put("userId", userId.toString());
      one.put("rating", reversiRating.getRating().toString());
      one.put("headIcon", user.getHeadIcon());
      one.put("username", user.getUsername());
      result.add(one);
    }
    return result;
  }
}
