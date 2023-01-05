package com.soku.rebotcorner.service.rating;

import cn.hutool.json.JSONObject;

public interface RatingService {
  JSONObject getTop10ByGameId(Integer gameId);

  JSONObject getMyRating(Integer gameId);
}
