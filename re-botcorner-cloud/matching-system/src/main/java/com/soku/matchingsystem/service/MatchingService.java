package com.soku.matchingsystem.service;

public interface MatchingService {
  String addPlayer(String game, Integer userId, Integer rating);
  String removePlayer(String game, Integer userId);
}
