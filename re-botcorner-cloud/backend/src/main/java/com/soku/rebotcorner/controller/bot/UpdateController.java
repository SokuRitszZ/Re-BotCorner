package com.soku.rebotcorner.controller.bot;

import com.soku.rebotcorner.service.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdateController {
  @Autowired
  private UpdateService updateService;

  @PostMapping("/bot/update")
  public Map<String, String> updateBot(@RequestParam Map<String, String> data) {
    return updateService.updateBot(data);
  }
}
