package com.soku.rebotcorner.games;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.consumer.SnakeWebSocketServer;
import com.soku.rebotcorner.utils.RT;
import com.soku.rebotcorner.utils.RecordDAO;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.utils.UserDAO;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Pair {
  int x;
  int y;
  Pair(int x, int y) {
    this.x = x;
    this.y = y;
  }
};

public class SnakeGame extends Thread {
  private int rows;
  private int cols;
  private int[][] g;
  private int innerWallsCount;
  private static int[] dx = { -1, 0, 1, 0 };
  private static int[] dy = { 0, 1, 0, -1 };
  private String mode;
  private SnakeWebSocketServer socket0;
  private SnakeWebSocketServer socket1;
  private Integer direction0 = -1;
  private Integer direction1 = -1;
  private int step = 0;
  private Deque<Pair> snake0;
  private Deque<Pair> snake1;
  private ReentrantLock lock = new ReentrantLock();
  private JSONObject recordJson;
  private List<int[]> steps;
  private boolean isOver;
  private boolean hasSaved;
  private Integer result;
  private boolean hasRunBot0 = false;
  private boolean hasRunBot1 = false;

  public SnakeGame(String mode, int rows, int cols, int innerWallsCount, SnakeWebSocketServer socket0, SnakeWebSocketServer socket1) {
    this.mode = mode;
    this.rows = rows;
    this.cols = cols;
    this.innerWallsCount = innerWallsCount;
    this.g = new int[rows][cols];
    this.socket0 = socket0;
    this.socket1 = socket1;
    this.snake0 = new LinkedList<>();
    this.snake1 = new LinkedList<>();
    this.snake0.addFirst(new Pair(this.rows - 2, 1));
    this.snake1.addFirst(new Pair(1, this.cols - 2));
    steps = new ArrayList<>();
    recordJson = new JSONObject();
    recordJson.put("userId0", socket0.getUser().getId());
    recordJson.put("userId1", socket1.getUser().getId());
    result = null;
    createMap();
  }

  public int[][] getG() { return g; }

  public void setG(int[][] g) {
    this.g = g;
  }

  public void setSteps(List<int[]> steps) {
    this.steps = steps;
  }

  public String getMode() { return mode; }

  public Integer getResult() { return result; }

  public void setResult(Integer result) { this.result = result; }

  public void setOver(boolean over) {
    isOver = over;
  }

  private boolean checkIsValid(int sx, int sy, int tx, int ty) {
    Queue<Pair> q = new LinkedList<>();
    q.add(new Pair(sx, sy));
    boolean[][] st = new boolean[this.rows][this.cols];
    st[sx][sy] = true;
    while (!q.isEmpty()) {
      Pair u = q.remove();
      for (int i = 0; i < 4; ++i) {
        int nx = u.x + dx[i];
        int ny = u.y + dy[i];
        if (nx < 0 || nx >= this.rows || ny < 0 || ny >= this.cols) {
          continue;
        }
        if (g[nx][ny] == 1 || st[nx][ny]) {
          continue;
        }
        if (nx == tx && ny == ty) {
          return true;
        }
        st[nx][ny] = true;
        q.add(new Pair(nx, ny));
      }
    }
    return false;
  }

  private boolean draw() {
    for (int i = 0; i < this.rows; ++i) {
      for (int j = 0; j < this.cols; ++j) {
        g[i][j] = 0;
      }
    }
    for (int i = 0; i < this.rows; ++i) {
      g[i][0] = g[i][this.cols - 1] = 1;
    }
    for (int i = 0; i < this.cols; ++i) {
      g[0][i] = g[this.rows - 1][i] = 1;
    }
    Random random = new Random();

    for (int i = 0; i < this.innerWallsCount / 2; ++i) {
      for (int j = 0; j < 1000; ++j) {
        int r = random.nextInt(this.rows);
        int c = random.nextInt(this.cols);
        if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1) {
          continue;
        }
        if ((r == this.rows - 2 && c == 1) || (r == 1 && c == this.cols - 2)) {
          continue;
        }
        g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
        break;
      }
    }
    return checkIsValid(this.rows - 2, 1, 1, this.cols - 2);
  }

  public void createMap() {
    for (int i = 0; i < 1000; ++i) {
      if (draw()) {
        break;
      }
    }
    int[][] initG = new int[rows][cols];
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        initG[i][j] = g[i][j];
      }
    }
    recordJson.put("map", initG);
  }

  public void setDirection0(Integer direction0) {
    lock.lock();
    try {
      this.direction0 = direction0;
    } finally {
      lock.unlock();
    }
  }

  public void setDirection1(Integer direction1) {
    lock.lock();
    try {
      this.direction1 = direction1;
    } finally {
      lock.unlock();
    }
  }

  public void moveSnake() {
    Integer d0 = direction0;
    Integer d1 = direction1;
    int[] oneStep = new int[]{ d0, d1 };
    if (!"record".equals(mode))
      steps.add(oneStep);
    setDirection0(-1);
    setDirection1(-1);
    Pair first0 = snake0.getFirst();
    Pair first1 = snake1.getFirst();
    ++step;
    boolean isIncreasing = checkIsIncreasing();
    if (!isIncreasing) {
      Pair last0 = snake0.getLast();
      Pair last1 = snake1.getLast();
      g[last0.x][last0.y] -= 1;
      g[last1.x][last1.y] -= 1;
      snake0.removeLast();
      snake1.removeLast();
    }
    int nx0 = first0.x + dx[d0], ny0 = first0.y + dy[d0];
    int nx1 = first1.x + dx[d1], ny1 = first1.y + dy[d1];
    Pair newFirst0 = new Pair(nx0, ny0);
    Pair newFirst1 = new Pair(nx1, ny1);
    g[nx0][ny0] += 1;
    g[nx1][ny1] += 1;
    String status0 = "move";
    String status1 = "move";
    snake0.addFirst(newFirst0);
    snake1.addFirst(newFirst1);
    if (g[nx0][ny0] == 2) {
      status0 = "die";
    }
    if (g[nx1][ny1] == 2) {
      status1 = "die";
    }
    JSONObject json = new JSONObject();
    json.put("action", "moveSnake");
    json.put("direction0", d0);
    json.put("direction1", d1);
    json.put("isIncreasing", isIncreasing);
    json.put("status0", status0);
    json.put("status1", status1);
    if ("single".equals(mode) || "record".equals(mode)) {
      socket0.sendMessage(json.toJSONString());
    } else if ("multi".equals(mode)) {
      socket0.sendMessage(json.toJSONString());
      socket1.sendMessage(json.toJSONString());
    }
    if ("die".equals(status0) || "die".equals(status1)) {
      gameOver(status0, status1);
    }
    hasRunBot0 = false;
    hasRunBot1 = false;
  }

  private void gameOver(String status0, String status1) {
    if ("die".equals(status0) && !"die".equals(status1)) {
      result = 1;
    } else if ("die".equals(status1) && !"die".equals(status0)) {
      result = 0;
    } else {
      result = -1;
    }
    isOver = true;
    if ("multi".equals(mode)) {
      User user0 = socket0.getUser();
      User user1 = socket1.getUser();
      if (result == 0) {
        user0.setRating(user0.getRating() + steps.size() / 2);
        user1.setRating(user1.getRating() - steps.size() / 2);
      } else if (result == 1) {
        user0.setRating(user0.getRating() - steps.size() / 2);
        user1.setRating(user1.getRating() + steps.size() / 2);
      }
      if (result != -1) {
        UserDAO.updateById(user0);
        UserDAO.updateById(user1);
      }
    }
  }

  private boolean checkIsIncreasing() {
    return step < 10 || step % 3 == 0;
  }

  public void saveRecord() {
    JSONObject json = new JSONObject();
    json.put("action", "saveRecord");
    json.put("hasSaved", hasSaved);
    if (!hasSaved) {
      hasSaved = true;
      Date now = new Date();
      Record record = new Record(null, recordJson.toJSONString(), socket0.getUser().getId(), socket1.getUser().getId(), now, 1, getResult());
      RecordDAO.add(record);
    }
    socket0.sendMessage(json.toJSONString());
    if ("multi".equals(mode)) {
      socket1.sendMessage(json.toJSONString());
    }
  }

  public String parseData() {
    StringBuilder map = new StringBuilder();

    for (int i = 0; i < this.rows; ++i) {
      for (int j = 0; j < this.cols; ++j) {
        map.append(g[i][j]);
      }
    }

    StringBuilder data = new StringBuilder();
    data.append(String.valueOf(rows) + " ");
    data.append(String.valueOf(cols) + " ");
    data.append(String.valueOf(step) + " ");
    data.append(map + " ");
    data.append(String.valueOf(snake0.size()) + " ");
    data.append(String.valueOf(snake1.size()) + " ");
    StringBuilder snake0Str = new StringBuilder();
    for (Pair pair: snake0) {
      snake0Str.append(String.valueOf(pair.x) + " ");
      snake0Str.append(String.valueOf(pair.y) + " ");
    }
    data.append(snake0Str);
    StringBuilder snake1Str = new StringBuilder();
    for (Pair pair: snake1) {
      snake1Str.append(String.valueOf(pair.x) + " ");
      snake1Str.append(String.valueOf(pair.y) + " ");
    }
    data.append(snake1Str);
    return data.toString();
  }

  public void runBot0() {
    hasRunBot0 = true;
    String data = String.valueOf(0) + " " + parseData();
    socket0.bot.prepareData(data);
    JSONObject json = JSONObject.parseObject(socket0.bot.run());
    String result = json.getString("data");
    setDirection0(Integer.parseInt(result));
    socket0.setDirection(0);
  }

  public void runBot1() {
    hasRunBot1 = true;
    String data = String.valueOf(1) + " " + parseData();
    socket1.bot.prepareData(data);
    JSONObject json = JSONObject.parseObject(socket1.bot.run());
    String result = json.getString("data");
    setDirection1(Integer.parseInt(result));
    socket1.setDirection(1);
  }

  @Override
  public void run() {
    g[rows - 2][1] = g[1][cols - 2] = 1;
    if (!"record".equals(mode)) {
      while (!isOver) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (direction0 != -1 && direction1 != -1) {
          lock.lock();
          moveSnake();
          lock.unlock();
        }
        if (isOver) break;
        if (direction0 == -1 && socket0.bot != null && !hasRunBot0) {
          new Thread(() -> {
            runBot0();
          }).start();
        }
        if (direction1 == -1 && socket1.bot != null && !hasRunBot1) {
          new Thread(() -> {
            runBot1();
          }).start();
        }
      }
      /** stop */
      recordJson.put("steps", steps);
    } else {
      while (!isOver && step < steps.size()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        Integer d0 = steps.get(step)[0];
        Integer d1 = steps.get(step)[1];
        if (isOver) break;
        setDirection0(d0);
        setDirection1(d1);
        moveSnake();
      }
    }
    if (socket0.bot != null) {
      socket0.bot.stop();
    }
    if (socket1.bot != null) {
      socket1.bot.stop();
    }
    if (step == steps.size()) {
      if ("record".equals(mode)) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      JSONObject resultJson = new JSONObject();
      resultJson.put("action", "tellResult");
      resultJson.put("result", result);
      if ("multi".equals(mode) && result != -1) {
        resultJson.put("score", steps.size() / 2);
      }
      socket0.sendMessage(resultJson.toJSONString());
      if ("multi".equals(mode))
        socket1.sendMessage(resultJson.toJSONString());
    }
  }
}
