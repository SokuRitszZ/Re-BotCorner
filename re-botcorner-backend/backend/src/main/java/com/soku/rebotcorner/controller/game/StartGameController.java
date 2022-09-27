package com.soku.rebotcorner.controller.game;

import com.soku.rebotcorner.service.game.StartGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartGameController {
  @Autowired
  private StartGameService startGameService;

  @PostMapping("/api/game/startgame")
  public String startGame(@RequestParam MultiValueMap<String, String> data) {
    return startGameService.startGame(data.getFirst("game"), Integer.parseInt(data.getFirst("userId0")), Integer.parseInt(data.getFirst("userId1")));
  }
}
