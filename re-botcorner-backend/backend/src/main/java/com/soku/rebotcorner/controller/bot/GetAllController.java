package com.soku.rebotcorner.controller.bot;

import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.service.bot.GetAllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GetAllController {
  @Autowired
  private GetAllService getAllService;

  @GetMapping("/api/bot/getAll")
  public List<Bot> getAll() {
    return getAllService.getAll();
  }
}
