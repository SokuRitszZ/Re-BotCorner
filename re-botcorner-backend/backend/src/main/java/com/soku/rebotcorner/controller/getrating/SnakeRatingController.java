package com.soku.rebotcorner.controller.getrating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.getrating.SnakeRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SnakeRatingController {
  @Autowired
  private SnakeRatingService snakeRatingService;

  @GetMapping("/api/getrating/snake")
  public JSONObject getRatingList() {
    return snakeRatingService.getRatingList();
  }
}
