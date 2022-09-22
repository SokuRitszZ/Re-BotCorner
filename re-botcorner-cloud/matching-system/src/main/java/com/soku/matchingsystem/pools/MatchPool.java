package com.soku.matchingsystem.pools;

public interface MatchPool {
  void addPlayer(Integer userId, Integer rating);
  void removePlayer(Integer userId);
  void start();
}
