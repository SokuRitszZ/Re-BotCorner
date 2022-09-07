package com.soku.matchingsystem.pools;

import com.soku.matchingsystem.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SnakePool extends Thread {
  private static List<Player> players = new ArrayList<>();
  private ReentrantLock lock = new ReentrantLock();
  private static RestTemplate restTemplate;
  private final static String startGameUrl = "http://localhost:8080/game/startgame/";

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    SnakePool.restTemplate = restTemplate;
  }

  public void addPlayer(Integer userId, Integer rating) {
    lock.lock();
    try {
      players.add(new Player(userId, rating, 0));
    } finally {
      lock.unlock();
    }
  }

  public void removePlayer(Integer userId) {
    lock.lock();
    try {
      List<Player> newPlayers = new ArrayList<>();
      for (Player player: players) {
        if (!player.getUserId().equals(userId)) {
          newPlayers.add(player);
        }
      }
      players = newPlayers;
    } finally {
      lock.unlock();
    }
  }

  private void increaseWaitingTime() {
    for (Player player: players) {
      player.setWatingTime(player.getWatingTime() + 1);
    }
  }

  private boolean checkMatched(Player a, Player b) {
    int ratingDelta = Math.abs(a.getRating() - b.getRating());
    int waitingTime = Math.min(a.getWatingTime(), b.getWatingTime());
    return waitingTime * 10 >= ratingDelta;
  }

  private void match() {
    boolean[] used = new boolean[players.size()];
    for (int i = 0; i < players.size(); ++i) {
      if (used[i]) continue;
      Player p0 = players.get(i);
      for (int j = i + 1; j < players.size(); ++j) {
        if (used[j]) continue;
        Player p1 = players.get(j);
        if (checkMatched(p0, p1)) {
          used[i] = used[j] = true;
          sendResult(p0, p1);
          break;
        }
      }
    }
    List<Player> newPlayers = new ArrayList<>();
    for (int i = 0; i < players.size(); ++i) {
      if (!used[i]) {
        newPlayers.add(players.get(i));
      }
    }
    players = newPlayers;
  }

  private void sendResult(Player a, Player b) {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "snake");
    data.add("userId0", a.getUserId().toString());
    data.add("userId1", b.getUserId().toString());
    restTemplate.postForObject(startGameUrl, data, String.class);
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(1000);
        lock.lock();
        try {
          increaseWaitingTime();
          match();
        } finally {
          lock.unlock();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
