package com.soku.rebotcorner.controller.bot;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddController {
  @Autowired
  private AddService addService;

  @PostMapping("/api/bot/add")
  public JSONObject addBot(@RequestBody JSONObject json) {
    return addService.addBot(json);
  }
}
