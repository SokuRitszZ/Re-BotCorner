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

  @PostMapping("/matching/add")
  public String addPlayer(@RequestParam MultiValueMap<String, String> data) {
    Integer userId = Integer.parseInt(data.getFirst("userId"));
    Integer rating = Integer.parseInt(data.getFirst("rating"));
    return matchingService.addPlayer(userId, rating);
  }

  @PostMapping("/matching/remove")
  public String removePlayer(@RequestParam MultiValueMap<String, String> data) {
    Integer userId = Integer.parseInt(data.getFirst("userId"));
    return matchingService.removePlayer(userId);
  }
}
