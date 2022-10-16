package com.soku.rebotcorner.controller.bot;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UpdateController {
  @Autowired
  private UpdateService updateService;

  @PostMapping("/api/bot/update")
  public Map<String, String> updateBot(@RequestBody JSONObject json) {
    Map<String, String> data = new HashMap<>();
    data.put("id", String.valueOf(json.getInteger("id")));
    data.put("title", json.getString("title"));
    data.put("description", json.getString("description"));
    data.put("code", json.getString("code"));
    return updateService.updateBot(data);
  }
}
