package com.soku.rebotcorner.games;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class Pair {
  int x;
  int y;
  Pair(int x, int y) {
    this.x = x;
    this.y = y;
  }
};
public class SnakeGame {
  private int rows;
  private int cols;
  private int[][] g;
  private int innerWallsCount;
  private static int[] dx = { -1, 0, 1, 0 };
  private static int[] dy = { 0, 1, 0, -1 };

  public SnakeGame(int rows, int cols, int innerWallsCount) {
    this.rows = rows;
    this.cols = cols;
    this.innerWallsCount = innerWallsCount;
    this.g = new int[rows][cols];
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
}
