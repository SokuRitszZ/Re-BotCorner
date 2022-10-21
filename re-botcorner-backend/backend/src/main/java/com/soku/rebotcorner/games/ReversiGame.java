package com.soku.rebotcorner.games;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.consumer.ReversiWebSocketServer;
import com.soku.rebotcorner.consumer.match.ReversiMatch;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.pojo.ReversiRating;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.BotDAO;
import com.soku.rebotcorner.utils.RecordDAO;
import com.soku.rebotcorner.utils.ReversiRatingDAO;
import com.soku.rebotcorner.utils.UserDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class ReversiGame extends Thread {
  private static int dx[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
  private static int dy[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

  private int rows;
  private int cols;
  private String mode;
  private int[][] chess;
  private int step;
  private List<String> steps;
  private boolean isGameOver;
  private ReversiMatch match;
  private int[] toPut;
  private UUID uuid;
  private boolean hasSavedRecord;
  private int gameResult;
  private RunningBot[] bot = new RunningBot[2];
  private String reason;
  private ReentrantLock lock = new ReentrantLock();

  public ReversiGame(String mode, int rows, int cols, ReversiWebSocketServer socket0, ReversiWebSocketServer socket1, RunningBot bot0, RunningBot bot1) {
    this.mode = mode;
    this.uuid = UUID.randomUUID();
    this.rows = rows;
    this.cols = cols;
    bot[0] = bot0;
    bot[1] = bot1;
    this.match = new ReversiMatch(new ReversiWebSocketServer[]{socket0, socket1});
    chess = new int[rows][cols];
    step = 0;
    steps = new ArrayList<>();
    isGameOver = false;

    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        chess[i][j] = 2;
      }
    }

    int midRow = rows >> 1;
    int midCol = cols >> 1;

    chess[midRow - 1][midCol - 1] = chess[midRow][midCol] = 1;
    chess[midRow][midCol - 1] = chess[midRow - 1][midCol] = 0;
  }

  public boolean isGameOver() {
    return isGameOver;
  }

  public void setToPut(Integer id, int[] toPut) {
    if ("multi".equals(mode) && step % 2 != id) return ;
    this.toPut = toPut;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public void setSteps(List<String> steps) {
    this.steps = steps;
  }

  public void setGameResult(int gameResult) {
    this.gameResult = gameResult;
  }

  public void setGameOver(boolean gameOver) {
    isGameOver = gameOver;
  }

  public boolean isIn(int r, int c) {
    return r >= 0 && c >= 0 && r < this.rows && c < this.cols;
  }

  public boolean isValidDir(int r, int c, int i) {
    int id = step % 2;
    int nr = r + dx[i];
    int nc = c + dy[i];
    boolean flg = false;
    while (isIn(nr, nc) && chess[nr][nc] == (id ^ 1)) {
      nr += dx[i];
      nc += dy[i];
      flg = true;
    }
    return flg && isIn(nr, nc) && chess[nr][nc] == id;
  }

  public boolean isValid(int r, int c) {
    int id = step % 2;
    if (!isIn(r, c) || chess[r][c] != 2) return false;
    for (int i = 0; i < 8; ++i) {
      if (isValidDir(r, c, i)) return true;
    }
    return false;
  }

  public void displayChess() {
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        System.out.print(chess[i][j] + " ");
      }
      System.out.println();
    }
  }

  public JSONObject putChess(int r, int c) {
    lock.lock();
    JSONObject data = new JSONObject();
    data.put("action", "putChess");
    if (!isValid(r, c)) {
      data.put("result", "非法操作");
      lock.unlock();
      return data;
    }
    int id = step % 2;
    chess[r][c] = id;
    for (int i = 0; i < 8; ++i) {
      if (isValidDir(r, c, i)) {
        int nr = r + dx[i];
        int nc = c + dy[i];
        while (isIn(nr, nc) && chess[nr][nc] == (id ^ 1)) {
          chess[nr][nc] = id;
          nr += dx[i];
          nc += dy[i];
        }
      }
    }
    data.put("result", "ok");
    data.put("id", step++ % 2);
    data.put("r", r);
    data.put("c", c);
    if (!"record".equals(mode)) steps.add(r + "," + c);
    lock.unlock();
    return data;
  }

  public JSONObject parseData() {
    JSONObject data = new JSONObject();
    data.put("rows", "" + rows);
    data.put("cols", "" + cols);
    String stringifiedChess = "";
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        stringifiedChess += chess[i][j];
      }
    }
    data.put("stringifiedChess", stringifiedChess);
    return data;
  }

  public String parseDataString() {
    String data = "";
    data += rows;
    data += " " + cols;
    String stringifiedChess = "";
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        stringifiedChess += chess[i][j];
      }
    }
    data += " " + stringifiedChess;
    return data;
  }

  public boolean checkIsOver() {
    int id = step % 2;
    boolean flag = true;
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        if (chess[i][j] == id) {
          flag = false;
       }
      }
    }
    if (flag) {
      gameResult = id ^ 1;
      reason = id == 0 ? "无黑子" : "无白子";
      return true;
    }
    int[] cnt = new int[2];
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        if (chess[i][j] == 2) {
          return false;
        }
        cnt[chess[i][j]]++;
      }
    }
    if (cnt[0] > cnt[1]) {
      reason = "黑子多于白子";
      gameResult = 0;
    } else if (cnt[1] > cnt[0]) {
      reason = "白子多于黑子";
      gameResult = 1;
    } else {
      reason = "黑白子相等";
      gameResult = -1;
    }
    return true;
  }

  public void settleUp() {
    if (mode == "multi" && gameResult != -1) {
      ReversiRating[] ratings = new ReversiRating[2];
      ratings[0] = ReversiRatingDAO.selectById(match.sockets[0].getUser().getId());
      ratings[1] = ReversiRatingDAO.selectById(match.sockets[1].getUser().getId());
      Integer winner = gameResult;
      ratings[winner].setRating(ratings[winner].getRating() + 5);
      ratings[1 - winner].setRating(ratings[1 - winner].getRating() - 5);
      ReversiRatingDAO.updateById(ratings[0]);
      ReversiRatingDAO.updateById(ratings[1]);
    }
  }

  public void gameOver() {
    isGameOver = true;
    JSONObject json = new JSONObject();
    settleUp();
    tellResult();
  }

  public boolean checkIsPassed() {
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        if (isValid(i, j)) {
          return false;
        }
      }
    }
    return true;
  }

  public void pass() {
    ++step;
    steps.add("passed");
    JSONObject json = new JSONObject();
    json.put("action", "pass");
    json.put("passTo", step % 2);
    match.broadcast(json);
  }

  public JSONObject saveRecord() {
    JSONObject data = new JSONObject();
    data.put("action", "saveRecord");
    if (hasSavedRecord) {
      data.put("saveResult", "录像已保存");
      return data;
    }
    hasSavedRecord = true;
    JSONObject json = new JSONObject();
    json.put("rows", rows);
    json.put("cols", cols);
    json.put("reason", reason);
    String stringifiedSteps = String.join(" ", steps);
    json.put("steps", stringifiedSteps);
    Integer userId0 = match.sockets[0].getUser().getId();
    Integer userId1 = match.sockets[1].getUser().getId();
    if (bot[0] != null) {
      userId0 = bot[0].getBot().getUserId();
    }
    if (bot[1] != null) {
      userId1 = bot[1].getBot().getUserId();
    }
    User user0 = UserDAO.selectById(userId0);
    User user1 = UserDAO.selectById(userId1);
    String username0 = user0.getUsername();
    String username1 = user1.getUsername();
    String headIcon0 = user0.getHeadIcon();
    String headIcon1 = user1.getHeadIcon();
    if (bot[0] != null) {
      username0 += "[" + bot[0].getBot().getTitle() + "]";
    }
    if (bot[1] != null) {
      username1 += "[" + bot[1].getBot().getTitle() + "]";
    }
    Record record = new Record(
      null,
      json.toJSONString(),
      new Date(),
      2
    );
    RecordDAO.add(record);
    data.put("saveResult", "ok");
    data.put("id", record.getId());
    data.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    return data;
  }

  public void playRecord() throws InterruptedException {
    for (String step: steps) {
      Thread.sleep(1000);
      if (isGameOver) return ;
      JSONObject json = new JSONObject();
      json.put("action", "putChess");
      if (!"passed".equals(step)) {
        int id = this.step % 2;
        String[] rc = step.split(",");
        int r = Integer.parseInt(rc[0]);
        int c = Integer.parseInt(rc[1]);
        json.put("result", "ok");
        json.put("id", id);
        json.put("r", r);
        json.put("c", c);
        putChess(r, c);
      } else {
        ++this.step;
        json.put("action", "pass");
        json.put("result", "pass");
        json.put("passTo", this.step % 2);
      }
      match.broadcast(json);
    }
    tellResult();
  }

  public void compileBot() {
    AtomicBoolean compiled0 = new AtomicBoolean(bot[0] == null);
    AtomicBoolean compiled1 = new AtomicBoolean(bot[1] == null);
    if (bot[0] != null) {
      new Thread(() -> {
        bot[0].start();
        bot[0].compile();
        compiled0.set(true);
      }).start();
    }
    if (bot[1] != null) {
      new Thread(() -> {
        bot[1].start();
        bot[1].compile();
        compiled1.set(true);
      }).start();
    }
    while (!compiled0.get() || !compiled1.get());
  }

  public void checkRunBot() throws InterruptedException {
    lock.lock();
    int id = step % 2;
    if (bot[id] != null) {
      Thread.sleep(200);
      String data = id + " " + parseDataString();
      bot[id].prepareData(data);
      JSONObject json = JSONObject.parseObject(bot[id].run());
      String result = json.getString("data");
      try {
        if (result == null || result.length() == 0) throw new RuntimeException("运行超时");
        result = result.trim();
        Pattern pattern = Pattern.compile("^((\\d)|([1-9]\\d*))\\s((\\d)|([1-9]\\d*))$");
        if (!pattern.matcher(result).matches()) {
          throw new RuntimeException(String.format("非法输出0: %s", result));
        }
        String[] rc = result.split(" ");
        int r = Integer.parseInt(rc[0]);
        int c = Integer.parseInt(rc[1]);
        if (r < 0 || r >= rows || c < 0 || c >= cols) throw new RuntimeException(String.format("非法输出1: %s", result));
        if (!isValid(r, c)) { throw new RuntimeException(String.format("非法输出2: %s", result)); }
        setToPut(id, new int[]{r, c});
      } catch (Exception e) {
        setGameResult(1 - id);
        reason = e.getMessage();
        gameOver();
      }
    }
    lock.unlock();
  }

  public void stopBot() {
    for (int i = 0; i < 2; ++i) {
      if (bot[i] != null) {
        bot[i].stop();
      }
    }
  }

  public void tellResult() {
    JSONObject json = new JSONObject();
    json.put("action", "gameOver");
    json.put("result", gameResult);
    json.put("reason", reason);
    match.broadcast(json);
  }

  public void run() {
    isGameOver = false;
    // 游戏还没结束
    while (!isGameOver) {
      // 忙等，还没有做下一步动作
      try {
        checkRunBot();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      if (isGameOver) break;
      if (toPut != null) {
        JSONObject json = putChess(toPut[0], toPut[1]);
        match.broadcast(json);
        toPut = null;
      }
      if (checkIsOver()) {
        gameOver();
        break;
      }
      if (checkIsPassed()) pass();
    }
    stopBot();
  }
}
