package com.soku.rebotcorner.service.impl.rating;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.RatingMapper;
import com.soku.rebotcorner.pojo.Rating;
import com.soku.rebotcorner.service.rating.RatingService;
import com.soku.rebotcorner.utils.GetUserByToken;
import com.soku.rebotcorner.utils.NewRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {
  @Autowired
  private RatingMapper mapper;

  @Override
  public JSONObject getTop(Integer gameId, Integer count) {
    List<JSONObject> top = mapper.getTop(gameId, count);
    return NewRes.ok(
      new JSONObject()
        .set("ratings", top)
    );
  }

  @Override
  public JSONObject getMyRating(Integer gameId) {
    Integer id = GetUserByToken.get().getId();
    Rating rating = mapper.selectOne(new QueryWrapper<Rating>().eq("user_id", id).eq("game_id", gameId));
    if (rating == null) {
      mapper.insert(
        rating = new Rating()
          .setGameId(gameId)
          .setScore(1500)
          .setUserId(id)
      );
    }
    return NewRes.ok(new JSONObject().set("rating", rating));
  }
}
