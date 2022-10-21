package com.soku.rebotcorner.service.impl.game;

import com.soku.rebotcorner.consumer.BackgammonWebSocketServer;
import com.soku.rebotcorner.consumer.GameSocketServer;
import com.soku.rebotcorner.consumer.ReversiWebSocketServer;
import com.soku.rebotcorner.consumer.SnakeWebSocketServer;
import com.soku.rebotcorner.pojo.Game;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.game.StartGameService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StartGameServiceImpl implements StartGameService {

  @Override
  public String startGame(String game, Integer userId0, Integer userId1) {
    this.startGame(Arrays.asList(new Integer[]{userId0, userId1}));
    return "Match OK";
  }

  public void startGame(List<Integer> userIds) {
    GameSocketServer.makeMatching(userIds);
  }
}
