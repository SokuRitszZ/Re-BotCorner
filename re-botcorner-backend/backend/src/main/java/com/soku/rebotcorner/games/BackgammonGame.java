package com.soku.rebotcorner.games;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.consumer.BackgammonWebSocketServer;
import com.soku.rebotcorner.runningbot.RunningBot;
import org.springframework.security.core.parameters.P;
import org.springframework.util.MultiValueMap;

import java.util.*;

public class BackgammonGame extends Thread {
  static class Pair {
    int type, count;
  };

  private static Random random = new Random();
  private String mode;
  private BackgammonWebSocketServer socket0, socket1;
  private RunningBot bot0, bot1;
  private Pair[] chess;
  private boolean isGameOver;
  private List<Integer> points = new ArrayList<>();

  public BackgammonGame(
    String mode,
    BackgammonWebSocketServer socket0,
    BackgammonWebSocketServer socket1,
    RunningBot bot0,
    RunningBot bot1
  ) {
    this.mode = mode;
    this.socket0 = socket0;
    this.socket1 = socket1;
    this.bot0 = bot0;
    this.bot1 = bot1;

    createChess();
  }

  private void createChess() {
    chess = new Pair[26];
    for (int i = 0; i < 26; ++i) {
      chess[i].type = 2;
      chess[i].count = 0;
    }
    // 老家是 0(白) / 25(红), 终点是 25(白), 0(红)
    chess[1].type = chess[12].type = chess[17].type = chess[19].type = 0;
    chess[6].type = chess[8].type = chess[13].type = chess[24].type = 1;
    chess[1].count = chess[24].count = 2;
    chess[8].count = chess[17].count = 3;
    chess[6].count = chess[12].count = chess[13].count = chess[19].count = 5;
  }

  private void createNewpoints() {
    Integer p0 = random.nextInt(6) + 1, p1 = random.nextInt(6) + 1;
    points.add(p0);
    points.add(p1);
    if (p0 == p1) {
      points.add(p0);
      points.add(p1);
    }
  }

  private Set<Integer> getValids() {
    int n = points.size();
    Set<Integer> result = new HashSet<>();
    for (int mask = 0; mask < 1 << n; ++mask) {
      int sum = 0;
      for (int i = 0; i < n; ++i) {
        if ((mask >> i & 1) == 1) {
          sum += points.get(i);
        }
      }
      result.add(sum);
    }
    return result;
  }

  private JSONObject moveChess(int id, int from, int to) {
    JSONObject json = new JSONObject();
    json.put("action", "moveChess");
    // 白棋
    if (id == 0) {
      // 从老家出来，不可能一步直接走到终点
      if (from == 0) {
        // 有敌人，且敌人是保护状态，不可行
        if (chess[to].type == 1 && chess[to].count > 1) {
          json.put("result", "非法操作");
          return json;
        } else if (chess[to].type != 1) {
          chess[to].count++;
          chess[from].count--;
          json.put("result", "success");
        } else if (chess[to].type == 1) {
          chess[25].count++;
          chess[to].type = 0;
        }
      }
      // 从不是老家出来
    }

    // 红棋
    if (id == 1) {

    }

    return null;
  }

  public JSONObject parseData() {
    return null;
  }

  public String parseDataString() {
    String result = "";
    for (int i = 0; i < 28; ++i) result += chess[i].type + " " + chess[i].count + " ";
    return result.strip();
  }

  public void run() {
    while (!isGameOver) {

    }
  }
}
