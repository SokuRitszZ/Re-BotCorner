package com.soku.rebotcorner.service.rating;

import cn.hutool.json.JSONObject;

public interface RatingService {
  JSONObject getTop(Integer gameId, Integer count);

  JSONObject getMyRating(Integer gameId);
}
