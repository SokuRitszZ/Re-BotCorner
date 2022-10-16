package com.soku.rebotcorner.controller.bot;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AddController {
  @Autowired
  private AddService addService;

  @PostMapping("/api/bot/add")
  public Map<String, String> addBot(@RequestBody JSONObject json) {
    Map<String, String> data = new HashMap<>();
    data.put("title", json.getString("title"));
    data.put("description", json.getString("description"));
    data.put("code", json.getString("code"));
    data.put("gameId", json.getString("gameId"));
    data.put("langId", json.getString("langId"));
    return addService.addBot(data);
  }
}
