package com.soku.matchingsystem.controller.pools;

public interface MatchPool {
  void addPlayer(Integer userId, Integer rating);
  void removePlayer(Integer userId);
  void start();
}
