package com.soku.rebotcorner.controller.rating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.rating.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/rating")
@RestController
public class RatingController {
  @Autowired
  private RatingService service;

  @GetMapping("/top")
  public JSONObject getTop10ByGameId(@RequestParam("gameId") Integer gameId, @RequestParam("count") Integer count) {
    return service.getTop(gameId, count);
  }

  @GetMapping("/my")
  public JSONObject getMyRating(@RequestParam("gameId") Integer gameId) {
    return service.getMyRating(gameId);
  }
}
