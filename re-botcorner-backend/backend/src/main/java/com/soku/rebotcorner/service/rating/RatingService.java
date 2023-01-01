package com.soku.rebotcorner.service.rating;

import cn.hutool.json.JSONObject;
import org.springframework.web.bind.annotation.RequestParam;

public interface RatingService {
  JSONObject getTop10ByGameId(@RequestParam("gameId") Integer gameId);
}
