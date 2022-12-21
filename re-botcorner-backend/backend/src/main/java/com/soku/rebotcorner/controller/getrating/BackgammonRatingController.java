package com.soku.rebotcorner.controller.getrating;

import cn.hutool.json.JSONObject;
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
  public JSONObject getRatingList() {
    return backgammonRatingService.getRatingList();
  }
}
