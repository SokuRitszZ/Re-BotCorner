package com.soku.matchingsystem.service.impl;

import com.soku.matchingsystem.pools.SnakePool;
import com.soku.matchingsystem.service.MatchingService;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {
  public final static SnakePool snakePool = new SnakePool();

  @Override
  public String addPlayer(Integer userId, Integer rating) {
    System.out.println(userId.toString() + " " + rating.toString());
    snakePool.addPlayer(userId, rating);
    return "add successfully";
  }

  @Override
  public String removePlayer(Integer userId) {
    System.out.println(userId);
    snakePool.removePlayer(userId);
    return "remove successfully";
  }
}
