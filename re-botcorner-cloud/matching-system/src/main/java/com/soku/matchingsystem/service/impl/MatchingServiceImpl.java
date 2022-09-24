package com.soku.matchingsystem.service.impl;

import com.soku.matchingsystem.pools.MatchPool;
import com.soku.matchingsystem.pools.ReversiPool;
import com.soku.matchingsystem.pools.SnakePool;
import com.soku.matchingsystem.service.MatchingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MatchingServiceImpl implements MatchingService {
  public final static Map<String, MatchPool> pools = new HashMap<>();
  static {
    pools.put("snake", new SnakePool());
    pools.put("reversi", new ReversiPool());

    for (MatchPool matchPool: pools.values()) {
      matchPool.start();
    }
  }

  @Override
  public String addPlayer(String game, Integer userId, Integer rating) {
    System.out.println(game + " " + userId + " " + rating);
    pools.get(game).addPlayer(userId, rating);
    return "add successfully";
  }

  @Override
  public String removePlayer(String game, Integer userId) {
    pools.get(game).removePlayer(userId);
    return "remove successfully";
  }
}
