package com.soku.rebotcorner.games;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.consumer.SnakeWebSocketServer;

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
    createMap();
  }

  public int[][] getG() { return g; }

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
    if ("single".equals(mode)) {
      socket0.sendMessage(json.toJSONString());
    } else {
      socket0.sendMessage(json.toJSONString());
      socket1.sendMessage(json.toJSONString());
    }
  }

  private boolean checkIsIncreasing() {
    return step < 10 || step % 3 != 0;
  }

  @Override
  public void run() {
    g[rows - 2][1] = g[1][cols - 2] = 1;
    while (true) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (direction0 != -1 && direction1 != -1) {
        lock.lock();
        moveSnake();
        lock.unlock();
      }
    }
  }
}
