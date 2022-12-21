package com.soku.rebotcorner.controller.getrating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.getrating.ReversiRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ReversiRatingController {
  @Autowired
  private ReversiRatingService reversiRatingService;

  @GetMapping("/api/getrating/reversi")
  public JSONObject getRatingList() {
    return reversiRatingService.getRatingList();
  }
}
