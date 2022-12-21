package com.soku.rebotcorner.controller.bot;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UpdateController {
  @Autowired
  private UpdateService updateService;

  @PutMapping("/api/bot/update")
  public JSONObject updateBot(@RequestBody JSONObject json) {
    return updateService.updateBot(json);
  }
}
