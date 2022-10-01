package com.soku.rebotcorner.controller.getrating;

import com.soku.rebotcorner.service.getrating.BackgammonRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BackgammonRatingController {
  @Autowired
  private BackgammonRatingService backgammonRatingService;

  @GetMapping("/api/getrating/backgammon")
  public List<Map<String, String>> getRatingList() {
    return backgammonRatingService.getRatingList();
  }
}
