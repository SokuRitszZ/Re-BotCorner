package com.soku.matchingsystem.service.impl;

import com.soku.matchingsystem.pools.SnakePool;
import com.soku.matchingsystem.service.MatchingService;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {
  public final static SnakePool snakePool = new SnakePool();

  @Override
  public String addPlayer(Integer userId, Integer rating) {
    snakePool.addPlayer(userId, rating);
    return "add successfully";
  }

  @Override
  public String removePlayer(Integer userId) {
    snakePool.removePlayer(userId);
    return "remove successfully";
  }
}
