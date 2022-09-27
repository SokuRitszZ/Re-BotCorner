package com.soku.rebotcorner.controller.bot;

import com.soku.rebotcorner.service.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AddController {
  @Autowired
  private AddService addService;

  @PostMapping("/api/bot/add")
  public Map<String, String> addBot(@RequestParam Map<String, String> data) {
    return addService.addBot(data);
  }
}
