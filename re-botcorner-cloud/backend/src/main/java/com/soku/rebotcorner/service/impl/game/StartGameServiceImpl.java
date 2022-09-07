package com.soku.rebotcorner.service.impl.game;

import com.soku.rebotcorner.consumer.SnakeWebSocketServer;
import com.soku.rebotcorner.service.game.StartGameService;
import org.springframework.stereotype.Service;

@Service
public class StartGameServiceImpl implements StartGameService {

  @Override
  public String startGame(String game, Integer userId0, Integer userId1) {
    if ("snake".equals(game))
      SnakeWebSocketServer.makeMatching(userId0, userId1);
    return "Match OK";
  }
}
