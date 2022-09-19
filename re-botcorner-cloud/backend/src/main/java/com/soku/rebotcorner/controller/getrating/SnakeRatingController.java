package com.soku.rebotcorner.controller.getrating;

import com.soku.rebotcorner.service.getrating.SnakeRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SnakeRatingController {
  @Autowired
  private SnakeRatingService snakeRatingService;

  @GetMapping("/api/getrating/snake")
  public List<Map<String, String>> getRatingList() {
    return snakeRatingService.getRatingList();
  }
}
