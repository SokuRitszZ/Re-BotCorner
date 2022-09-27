import java.io.*;

class Main {
  private static final int[] dx = { -1, -1, 0, 1, 1, 1, 0, -1 };
  private static final int[] dy = { 0, 1, 1, 1, 0, -1, -1, -1 };
  private static int id, rows, cols;
  private static int[][] chess;

  public static void main(String[] args) throws Exception {
    String[] params = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");
    String __s;
    id = Integer.parseInt(params[0]);
    rows = Integer.parseInt(params[1]);
    cols = Integer.parseInt(params[2]);
    __s = params[3];
    chess = new int[rows][cols];
    for (int i = 0, k = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        chess[i][j] = Integer.parseInt(String.valueOf(__s.charAt(k++)));
      }
    }
    int x = 0, y = 0;

    // 在这里填写你的代码

    System.out.println(x + " " + y);
  }
}