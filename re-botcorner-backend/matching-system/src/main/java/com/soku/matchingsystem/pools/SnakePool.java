package com.soku.matchingsystem.pools;

import com.soku.matchingsystem.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SnakePool extends Thread implements MatchPool {
  private static Map<Integer, Player> players = new HashMap();
  private ReentrantLock lock = new ReentrantLock();
  private static RestTemplate restTemplate;
  private final static String startGameUrl = "http://localhost:8080/api/game/startgame/";

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    SnakePool.restTemplate = restTemplate;
  }

  public void addPlayer(Integer userId, Integer rating) {
    lock.lock();
    try {
      players.put(userId, new Player(userId, rating, 0));
    } finally {
      lock.unlock();
    }
    if (!this.isAlive()) {
      this.start();
    }
    display();
  }

  public void removePlayer(Integer userId) {
    lock.lock();
    try {
      players.remove(userId);
    } finally {
      lock.unlock();
    }
    if (!this.isAlive()) this.start();
    display();
  }

  private void increaseWaitingTime() {
    for (Player player: players.values()) player.setWatingTime(player.getWatingTime() + 1);
  }

  private boolean checkMatched(Player a, Player b) {
    int ratingDelta = Math.abs(a.getRating() - b.getRating());
    int waitingTime = Math.min(a.getWatingTime(), b.getWatingTime());
    return waitingTime * 10 >= ratingDelta;
  }

  private void match() {
    Collection<Player> values = players.values();
    List<Player> list = new ArrayList<>();
    for (Player current : values) {
      boolean ok = false;
      for (Player before : list) if (checkMatched(before, current)) {
        Integer beforeId = before.getUserId();
        Integer currentId = current.getUserId();
        players.remove(beforeId);
        players.remove(currentId);
        list.remove(before);
        sendResult(before, current);
        ok = true;
        break;
      }
      if (!ok) list.add(current);
    }
  }

  private void sendResult(Player a, Player b) {
    Random random = new Random();
    if (random.nextBoolean()) { Player t = new Player(a); a = new Player(b); b = t; }
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "snake");
    data.add("userId0", a.getUserId().toString());
    data.add("userId1", b.getUserId().toString());
    restTemplate.postForObject(startGameUrl, data, String.class);
  }

  public void display() {
    System.out.print("Snake Pool: [ ");
    for (Player player: players.values()) {
      System.out.print(player.getUserId() + " ");
    }
    System.out.print("]");
    System.out.println();
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
