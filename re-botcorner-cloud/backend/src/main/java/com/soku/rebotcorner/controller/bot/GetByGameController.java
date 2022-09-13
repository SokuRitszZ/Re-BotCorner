package com.soku.rebotcorner.controller.bot;

import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.service.bot.GetByGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GetByGameController {
  @Autowired
  private GetByGameService getByGameService;

  @GetMapping("/bot/getByGame")
  List<Bot> getByGame(@RequestParam Map<String, String> data) {
    return getByGameService.getByGame(data);
  }
}
