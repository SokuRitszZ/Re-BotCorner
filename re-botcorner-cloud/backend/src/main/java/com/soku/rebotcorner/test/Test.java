package com.soku.rebotcorner.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test {
  private final static int dx[] = {-1, 0, 1, 0};
  private final static int dy[] = {0, 1, 0, -1};
  static class Pair {
    int x;
    int y;
    Pair(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
  private static int id, rows, cols, step;
  private static int[] len = new int[2];
  private static int[][] g;
  private static List<Pair>[] snake = new List[2];

  public static void main(String[] args) {
    Scanner jin = new Scanner(System.in);
    id = jin.nextInt();
    rows = jin.nextInt();
    cols = jin.nextInt();
    step = jin.nextInt();
    String mapStr = jin.next();
    len[0] = jin.nextInt();
    len[1] = jin.nextInt();
    for (int i = 0; i < 2; ++i) {
      snake[i] = new ArrayList<>();
      for (int j = 0; j < len[i]; ++j) {
        int x = jin.nextInt();
        int y = jin.nextInt();
        snake[i].add(new Pair(x, y));
      }
    }
    jin.close();
    g = new int[rows][cols];
    for (int i = 0, k = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        g[i][j] = Integer.parseInt(String.valueOf(mapStr.charAt(k++)));
      }
    }
    int direction = 0;
    // start typing your code
    List<Integer> cango = new ArrayList<>();
    Pair head = snake[id].get(0);
    for (int i = 0; i < 4; ++i) {
      int nx = head.x + dx[i];
      int ny = head.y + dy[i];
      if (nx < rows && ny < cols && nx >= 0 && ny >= 0 && g[nx][ny] != 1) {
        cango.add(i);
      }
    }
    if (cango.size() > 0) {
      direction = cango.get(0);
    }
    // ok
    System.out.println(direction);
  }
}
