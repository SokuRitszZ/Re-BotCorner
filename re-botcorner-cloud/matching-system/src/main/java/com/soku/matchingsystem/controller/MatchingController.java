package com.soku.matchingsystem.controller;

import com.soku.matchingsystem.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchingController {
  @Autowired
  private MatchingService matchingService;

  @PostMapping("/api/matching/add")
  public String addPlayer(@RequestParam MultiValueMap<String, String> data) {
    Integer userId = Integer.parseInt(data.getFirst("userId"));
    Integer rating = Integer.parseInt(data.getFirst("rating"));
    String game = data.getFirst("game");
    return matchingService.addPlayer(game, userId, rating);
  }

  @PostMapping("/api/matching/remove")
  public String removePlayer(@RequestParam MultiValueMap<String, String> data) {
    Integer userId = Integer.parseInt(data.getFirst("userId"));
    String game = data.getFirst("game");
    return matchingService.removePlayer(game, userId);
  }
}
