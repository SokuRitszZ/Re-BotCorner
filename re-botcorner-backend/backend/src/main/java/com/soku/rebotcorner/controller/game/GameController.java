package com.soku.rebotcorner.controller.game;

import com.soku.rebotcorner.mapper.GameMapper;
import com.soku.rebotcorner.pojo.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController {
  @Autowired
  private GameMapper gameMapper;

  @GetMapping("/api/game/getAll")
  public List<Game> getAll() {
    return gameMapper.selectList(null);
  }
}
