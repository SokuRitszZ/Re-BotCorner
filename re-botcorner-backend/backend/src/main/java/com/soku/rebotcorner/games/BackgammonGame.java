package com.soku.rebotcorner.games;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.consumer.match.BackgammonMatch;
import com.soku.rebotcorner.pojo.BackgammonRating;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.BackgammonRatingDAO;
import com.soku.rebotcorner.utils.RecordDAO;
import com.soku.rebotcorner.utils.UserDAO;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class BackgammonGame extends Thread {
  static class Pair {
    int type, count;
  };

  private static Random random = new Random();
  private String mode;
  private BackgammonMatch match;
  public RunningBot bot0, bot1;
  private Pair[] chess;
  private boolean isGameOver;
  private List<Integer> dice = new ArrayList<>();
  private int curId;
  private int[] inHome = new int[]{0, 0};
  private boolean hasMove;
  private int winner = -1;
  private String result = null;
  private int score = 0;
  private List<String> steps = new ArrayList<>();
  private boolean hasSavedRecord = false;

  public BackgammonGame(
    String mode,
    BackgammonMatch match,
    RunningBot bot0,
    RunningBot bot1
  ) {
    this.mode = mode;
    this.match = match;
    this.bot0 = bot0;
    this.bot1 = bot1;
    createChess();
  }

  private void createChess() {
    chess = new Pair[26];
    for (int i = 0; i < 26; ++i) {
      chess[i] = new Pair();
      chess[i].type = 2;
      chess[i].count = 0;
    }
    // 老家是 0(白) / 25(红), 终点是 25(白), 0(红)
    chess[0].type = chess[1].type = chess[12].type = chess[17].type = chess[19].type = 0;
    chess[6].type = chess[8].type = chess[13].type = chess[24].type = chess[25].type = 1;
    chess[1].count = chess[24].count = 2;
    chess[8].count = chess[17].count = 3;
    chess[6].count = chess[12].count = chess[13].count = chess[19].count = 5;
  }

  private void getDice() {
    dice.clear();
    Integer p0 = random.nextInt(6) + 1, p1 = random.nextInt(6) + 1;
    dice.add(p0);
    dice.add(p1);
    if (p0 == p1) {
      dice.add(p0);
      dice.add(p1);
    }
  }

  private JSONObject setDice() {
    JSONObject json = new JSONObject();
    json.put("action", "setDice");
    json.put("dice", dice);
    json.put("curId", curId);
    return json;
  }

  public void setGameOver(boolean gameOver) {
    isGameOver = gameOver;
  }

  private boolean use(int step) {
    for (Integer d: dice) if (d == step) return true;
    return false;
  }

  public void moveChess(int id, int from, int to) {
    if (isGameOver) return ;
    // 添加移动的步
    steps.add("move " + id + " " + from + " " + to);
    if (id == 0) {
      --chess[from].count;
      if (from != 0 && chess[from].count == 0) chess[from].type = 2;
      if (to == 25) ++inHome[0];
      else if (chess[to].type == 0) ++chess[to].count;
      else if (chess[to].type == 1) {
        chess[to].type = 0;
        ++chess[25].count;
      } else if (chess[to].type == 2) {
        chess[to].type = 0;
        ++chess[to].count;
      }
      int step = to - from;
      boolean flag = false;
      for (int i = 0; i < dice.size(); ++i)
        if (dice.get(i) == step) {
          dice.remove(i);
          flag = true;
          break;
        }
      if (!flag) {
        for (int i = 0; i < dice.size(); ++i)
          if (dice.get(i) > step) {
            dice.remove(i);
            break;
          }
      }
      JSONObject ret = new JSONObject();
      ret.put("action", "moveChess");
      ret.put("result", "success");
      ret.put("from", from);
      ret.put("to", to);
      match.broadcast(ret);
      match.broadcast(setDice());
      hasMove = true;
    } else {
      --chess[from].count;
      if (from != 25 && chess[from].count == 0) chess[from].type = 2;
      if (to == 0) ++inHome[1];
      else if (chess[to].type == 1) ++chess[to].count;
      else if (chess[to].type == 0) {
        chess[to].type = 1;
        ++chess[0].count;
      }
      else if (chess[to].type == 2) {
        chess[to].type = 1;
        ++chess[to].count;
      }
      int step = from - to;
      boolean flag = false;
      for (int i = 0; i < dice.size(); ++i)
        if (dice.get(i) == step) {
          dice.remove(i);
          flag = true;
          break;
        }
      if (!flag) {
        for (int i = 0; i < dice.size(); ++i)
          if (dice.get(i) > step) {
            dice.remove(i);
            break;
          }
      }
      JSONObject ret = new JSONObject();
      ret.put("action", "moveChess");
      ret.put("result", "success");
      ret.put("from", from);
      ret.put("to", to);
      match.broadcast(ret);
      match.broadcast(setDice());
    }
    // 添加骰子的变化
    String diceString = "dice " + curId + " ";
    for (Integer d: dice) diceString += d + " ";
    steps.add(diceString.trim());
    new Thread(() -> {
      nextStep();
    }).start();
  }

  public void nextStep() {
    if (checkOver()) {
      tellResult();
      return ;
    }
    if (checkTurn()) turn();
    while (checkPass()) pass();
    checkRunBot();
  }

  public String checkInput(String input) {
    if (input == null) return "运行超时";
    input = input.trim();
    if (input.length() == 0) return "输出为空";
    Pattern pattern = Pattern.compile("^((\\d)|([1-9]\\d*))\\s((\\d)|([1-9]\\d*))$");
    if (!pattern.matcher(input).matches())
      return String.format("非法输出0: %s", input);
    String[] rc = input.split(" ");
    int from = Integer.parseInt(rc[0]);
    int to = Integer.parseInt(rc[1]);
    if (from < 0 || from >= 26 || to < 0 || to >= 26)
      return String.format("非法输出1: %s", input);
    if (!checkMoveChess(curId, from, to))
      return String.format("非法操作: %s", input);
    return "ok";
  }

  private void checkRunBot() {
    if (isGameOver) return ;
    if (curId == 0 && bot0 != null) {
      bot0.prepareData(parseDataString2());
      JSONObject json = JSONObject.parseObject(bot0.run());
      String input = json.getString("data");
      String runResult = checkInput(input);
      if (runResult != "ok") gameOver(runResult, 1 - curId);
      else {
        String[] rc = input.split(" ");
        int from = Integer.parseInt(rc[0]);
        int to = Integer.parseInt(rc[1]);
        try { sleep(200); } catch (InterruptedException e) { throw new RuntimeException(e); }
        moveChess(curId, from, to);
      }
    } else if (curId == 1 && bot1 != null) {
      bot1.prepareData(parseDataString2());
      JSONObject json = JSONObject.parseObject(bot1.run());
      String input = json.getString("data");
      String runResult = checkInput(input);
      if (runResult != "ok")
        gameOver(runResult, 1 - curId);
      else {
        String[] rc = input.split(" ");
        int from = Integer.parseInt(rc[0]);
        int to = Integer.parseInt(rc[1]);
        try { sleep(200); } catch (InterruptedException e) { throw new RuntimeException(e); }
        moveChess(curId, from, to);
      }
    }
  }

  // id 棋盘 骰子数 骰子点数
  private String parseDataString2() {
    String data = curId + " ";
    for (int i = 0; i < 26; ++i)
      data += chess[i].type + " " + chess[i].count + " ";
    data += dice.size() + " ";
    for (Integer d: dice)
      data += d + " ";
    return data.trim();
  }

  public boolean checkMoveChess(int id, int from, int to) {
    if (isGameOver) return false;
    if (id != curId) return false;
    boolean fail = false, success = true;
    int step = to - from;
    if (id == 1) step *= -1;
    if (((id == 0 && to != 25) || (id == 1 && to != 0)) && !use(step)) return fail;
    if (id == 0) {
      if (chess[from].type != 0 || chess[from].count == 0) return fail;
      if (chess[0].count > 0 && from != 0) return fail;
      if (to == 25) {
        for (int i = 1; i <= 18; ++i) if (chess[i].type == 0) return fail;
        if (!use(step)) {
          boolean flag = false;
          for (int i = 19; i < from; ++i) if (chess[i].type == 0) return fail;
          for (Integer d: dice) if (d > step) {
            flag = true;
            break;
          }
          if (!flag) return fail;
        }
      }
      if (chess[to].type == 1 && chess[to].count > 1) return fail;
      return success;
    } else if (id == 1) {
      if (chess[from].type != 1 || chess[from].count == 0) return fail;
      if (chess[25].count > 0 && from != 25) return fail;
      if (to == 0) {
        for (int i = 7; i <= 24; ++i) if (chess[i].type == 1) return fail;
        if (!use(step)) {
          for (int i = 6; i > from; --i) if (chess[i].type == 1) return fail;
          boolean flag = false;
          for (Integer d: dice) if (d > step) {
            flag = true;
            break;
          }
          if (!flag) return fail;
        }
      }
      if (chess[to].type == 0 && chess[to].count > 1) return fail;
      return success;
    } else return fail;
  }

  private boolean checkInner(int id) {
    if (id == 0) {
      for (int i = 19; i <= 24; ++i)
        if (chess[i].type == 1)
          return true;
    } else {
      for (int i = 1; i <= 6; ++i)
        if (chess[i].type == 0)
          return true;
    }
    return false;
  };

  private boolean checkOver() {
    if (isGameOver) return true;
    if (inHome[0] == 15) {
      winner = 0;
      if (inHome[1] == 0) {
        if (chess[25].count > 0 || checkInner(0)) {
          result = "完胜";
          this.score = 9;
        } else {
          result = "全胜";
          this.score = 6;
        }
      } else {
        result = "单胜";
        this.score = 3;
      }
    } else if (inHome[1] == 15) {
      winner = 1;
      if (inHome[0] == 0) {
        if (chess[0].count > 0 || checkInner(1)) {
          result = "完胜";
          this.score = 9;
        } else {
          result = "全胜";
          this.score = 6;
        }
      } else {
        result = "单胜";
        this.score = 3;
      }
    }
    if (winner != -1) {
      isGameOver = true;
      return true;
    }
    return false;
  }

  private boolean checkPass() {
    for (int i = 0; i < 26; ++i) {
      if (curId == 0 && i == 25) continue;
      if (curId == 1 && i == 0) continue;
      for (Integer d: dice) {
        int from = i;
        int to = i + (curId == 0 ? d : -d);
        if (to < 0) to = 0;
        if (to > 25) to = 25;
        if (checkMoveChess(curId, from, to)) return false;
      }
    }
    return true;
  }

  private void pass() {
    getDice();
    curId = 1 - curId;
    match.broadcast(setDice());
    JSONObject json = new JSONObject();
    json.put("action", "pass");
    json.put("passed", 1 - curId);
    String diceString = "dice " + curId + " ";
    for (Integer d: dice) diceString += d + " ";
    steps.add("pass");
    steps.add(diceString.trim());
    match.broadcast(json);
  }

  private boolean checkTurn() {
    return dice.size() == 0;
  }

  private void turn() {
    getDice();
    curId = 1 - curId;
    match.broadcast(setDice());
    String diceString = "dice " + curId + " ";
    for (Integer d: dice) diceString += d + " ";
    steps.add("turn");
    steps.add(diceString.trim());
  }

  public void compileBot() throws InterruptedException {
    AtomicInteger ready = new AtomicInteger();
    if (bot0 != null) {
      new Thread(() -> {
        bot0.start();
        bot0.compile();
        ready.getAndIncrement();
      }).start();
    } else {
      ready.getAndIncrement();
    }
    if (bot1 != null) {
      new Thread(() -> {
        bot1.start();
        bot1.compile();
        ready.getAndIncrement();
      }).start();
    } else {
      ready.getAndIncrement();
    }
    while (true) {
      sleep(1000);
      if (ready.get() == 2)
        break;
    }
  }

  public void gameOver(String result, int winner) {
    if (this.isGameOver) return ;
    this.isGameOver = true;
    this.result = result;
    this.winner = winner;
    if (result != "单胜" && result != "全胜" && result != "完胜") score = 2;
    if (this.mode == "multi") {
      BackgammonRating rating0 = BackgammonRatingDAO.selectById(match.sockets[0].getUser().getId());
      BackgammonRating rating1 = BackgammonRatingDAO.selectById(match.sockets[1].getUser().getId());
      if (winner == 0) {
        rating0.setRating(rating0.getRating() + score);
        rating1.setRating(rating1.getRating() - score);
      } else {
        rating1.setRating(rating1.getRating() + score);
        rating0.setRating(rating0.getRating() - score);
      }
      BackgammonRatingDAO.updateById(rating0);
      BackgammonRatingDAO.updateById(rating1);
    }
    tellResult();
  }

  private void tellResult() {
    if (bot0 != null) bot0.stop();
    if (bot1 != null) bot1.stop();
    JSONObject json = new JSONObject();
    json.put("action", "tellResult");
    json.put("winner", winner);
    json.put("result", result);
    match.broadcast(json);
    steps.add("result " + winner + " " + result);
  }

  public JSONObject saveRecord() {
    JSONObject ret = new JSONObject();
    ret.put("action", "saveRecord");
    if (hasSavedRecord) {
      ret.put("message", "录像已经保存");
      return ret;
    }
    hasSavedRecord = true;
    ret.put("message", "录像成功保存");
    JSONObject json = new JSONObject();
    json.put("result", winner);
    json.put("steps", steps);
    String jsonString = json.toJSONString();
    int userId0 = match.sockets[0].getUser().getId();
    int userId1 = match.sockets[1].getUser().getId();
    if (bot0 != null) userId0 = bot0.getBot().getUserId();
    if (bot1 != null) userId1 = bot1.getBot().getUserId();
    User user0 = UserDAO.selectById(userId0);
    User user1 = UserDAO.selectById(userId1);
    String username0 = user0.getUsername();
    String username1 = user1.getUsername();
    if (bot0 != null) username0 += String.format("[%s]", bot0.getBot().getTitle());
    if (bot1 != null) username1 += String.format("[%s]", bot1.getBot().getTitle());
    Record record = new Record(
      null,
      jsonString,
      new Date(),
      3
    );
    try {
      RecordDAO.add(record);
      ret.put("isOk", "ok");
      ret.put("json", jsonString);
      ret.put("userId0", userId0);
      ret.put("userId1", userId1);
      ret.put("username0", username0);
      ret.put("username1", username1);
      ret.put("headIcon0", user0.getHeadIcon());
      ret.put("headIcon1", user1.getHeadIcon());
      ret.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getCreateTime()));
      if (result == "单胜" || result == "全胜" || result == "完胜") ret.put("result", (winner == 0 ? "白子" : "红子") + " 输家" + result);
      else ret.put("result", (winner == 0 ? "白子" : "红子") + " " + result);
      return ret;
    } catch (Exception e) {
      ret.put("message", "内容过大，此录像无法保存");
      return ret;
    }
  }

  public JSONObject parseData() {
    return null;
  }

  public String parseDataString() {
    String result = "";
    for (int i = 0; i < 26; ++i) result += chess[i].type + " " + chess[i].count + " ";
    result += dice.size() + " ";
    return result.trim();
  }

  public void run() {
    getDice();
    while (dice.get(0) == dice.get(1)) getDice();
    if (dice.get(0) > dice.get(1)) curId = 0;
    else curId = 1;
    steps.add("start " + curId + " " + parseDataString());
    JSONObject json = setDice();
    json.put("start", curId);
    match.broadcast(json);
    checkRunBot();
  }
}