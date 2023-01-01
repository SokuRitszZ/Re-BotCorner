package com.soku.rebotcorner.service.impl.rating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.RatingMapper;
import com.soku.rebotcorner.service.rating.RatingService;
import com.soku.rebotcorner.utils.NewRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {
  @Autowired
  private RatingMapper mapper;

  @Override
  public JSONObject getTop10ByGameId(Integer gameId) {
    List<JSONObject> top10 = mapper.getTop10(gameId);
    return NewRes.ok(
      new JSONObject()
        .set("ratings", top10)
    );
  }
}
