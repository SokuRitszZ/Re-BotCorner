package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.soku.rebotcorner.utils.ScaleUtil.transform;

public class HexGame extends AbsGame {
  private static int[] dx = new int[]{-1, -1, 0, 1, 1, 0};
  private static int[] dy = new int[]{0, 1, 1, 0, -1, -1};

  private int rows = 11, cols = 11;
  private int[][] g = new int[rows][cols];
  private int step = 0;
  private int cur = 0;
  private int[] xy = new int[2];
  private boolean[][] visit = new boolean[rows][cols];
  private AtomicInteger ok = new AtomicInteger();

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public HexGame(String mode, GameMatch match, List<RunningBot> bots) {
    super(mode, match, bots);

    setGameId(4);

    // initG
    for (int[] ints : g) Arrays.fill(ints, 2);
  }

  @Override
  protected JSONObject makeInitData() {
    int rc = 0;
    rc |= (rows & (1 << 16) - 1);
    rc <<= 16;
    rc |= (cols & (1 << 16) - 1);
    StringBuilder mask = new StringBuilder();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        mask.append(g[i][j]);
      }
    }
    setInitData(new JSONObject()
      .set("rc", rc)
      .set("mask", mask.toString()));

    return getInitData();
  }

  @Override
  public void start() {
    setHasStart(true);

    ok.getAndIncrement();
    new Thread(() -> {
      while (!checkOver()) {
        if (ok.get() > 0) {
          ok.decrementAndGet();
          runBot();
        }
      }
    }).start();
  }

  @Override
  public String parseDataString() {
    StringBuilder data = new StringBuilder().append(cur).append(" ").append(step).append(" ");
    data.append(rows).append(" ").append(cols).append("\n");
    StringBuilder g = new StringBuilder();
    for (int[] ints : this.g)
      for (int anInt : ints)
        g.append(anInt).append(" ");
    data.append(g).append(xy[0]).append(" ").append(xy[1]);
    return data.toString().trim();
  }

  @Override
  protected void _setStep(JSONObject json) {
    Integer id = json.getInt("id");
    Integer x = json.getInt("x");
    Integer y = json.getInt("y");

    if (!beforeSetStep(id, x, y)) return;
    putChess();
    afterSetStep();
  }

  @Override
  public void describeResult() {
    // 有可能是因为中途退出了
    if (getResult() == null || getResult().length() == 0) {
      if (getReason()[0] == null || getReason()[0].length() == 0)
        setResult("红方不战而胜");
      else
        setResult("蓝方不战而胜");
    }
    getRecord().set("result", getResult());
  }

  @Override
  public Integer getPlayerCount() {
    return 2;
  }

  private boolean checkOver() {
    return xy[0] == -1 || xy[0] == 99;
  }

  private void runBot() {
    RunningBot bot = getBots().get(cur);
    if (bot != null) {
      bot.prepareData(parseDataString());
      JSONObject json = JSONUtil.parseObj(bot.run());
      String input = json.getStr("data");
      String runResult = checkInput(input);
      if (runResult != "ok") {
        setReason(cur, runResult);
        _setStep(
          new JSONObject()
            .set("id", cur)
            .set("x", -1)
            .set("y", -1)
        );
      } else {
        String[] rc = input.split(" ");
        int from = Integer.parseInt(rc[0]);
        int to = Integer.parseInt(rc[1]);
        _setStep(
          new JSONObject()
            .set("id", cur)
            .set("x", from)
            .set("y", to)
        );
      }
    }
  }

  private boolean beforeSetStep(int id, int x, int y) {
    xy[0] = x;
    xy[1] = y;
    return checkValid(id, x, y);
  }

  private void putChess() {
    if (!beforePutChess()) return;

    g[xy[0]][xy[1]] = cur;

    afterPutChess();
  }

  private void afterSetStep() {
    if (checkOver()) {
      gameOver();
      return;
    }

    cur ^= 1;
    ++step;

    ok.getAndIncrement();
  }

  private String checkInput(String input) {
    if (input == null) return "运行超时";
    input = input.trim();
    if (input.length() == 0) return "输出为空";
    Pattern pattern = Pattern.compile("^((\\d)|([1-9]\\d*))\\s((\\d)|([1-9]\\d*))$");
    if (!pattern.matcher(input).matches())
      return String.format("非法输出0: %s", input);
    String[] xy = input.split(" ");
    int x = Integer.parseInt(xy[0]);
    int y = Integer.parseInt(xy[1]);
    if (!isIn(x, y))
      return String.format("非法输出1: %s", input);
    if (!checkValid(cur, x, y))
      return String.format("非法操作: %s", input);
    return "ok";
  }

  private boolean checkValid(int id, int x, int y) {
    if (id != cur) return false;
    if (x == -1) return true;
    if (step == 0) return x == 1 && y == 2;
    return g[x][y] == 2;
  }

  private boolean beforePutChess() {
    return !checkOver();
  }

  private void afterPutChess() {
    addStep_put();

    // 检测游戏是否结束
    if (checkLink(xy[0], xy[1]) != 3) return;

    StringBuilder result = new StringBuilder();
    result.append(cur == 1 ? "蓝方" : "红方").append("获胜");
    setResult(result.toString());
    setReason(cur ^ 1, "战败");

    xy[0] = 99;
  }

  private int checkLink(int x, int y) {
    int res = 0;
    visit[x][y] = true;
    if (cur == 0) {
      if (x == 0) res |= 1;
      if (x == 10) res |= 2;
    } else if (cur == 1) {
      if (y == 0) res |= 1;
      if (y == 10) res |= 2;
    }
    for (int i = 0; i < 6; ++i) {
      int nx = x + dx[i];
      int ny = y + dy[i];
      if (!isIn(nx, ny)) continue;
      if (visit[nx][ny]) continue;
      if (g[nx][ny] != g[x][y]) continue;
      res |= checkLink(nx, ny);
    }
    visit[x][y] = false;
    return res;
  }

  private void addStep_put() {
    StringBuilder step = new StringBuilder();
    step.append(transform(xy[0], 36)).append(transform(xy[1], 36));
    getSteps().append(step);
    getMatch().broadCast(
      new JSONObject()
        .set("action", "set step truly")
        .set("data",
          new JSONObject()
            .set("step", step.toString()))
    );
  }

  private boolean isIn(int x, int y) {
    return 0 <= x && x < 11 && 0 <= y && y < 11;
  }
}
