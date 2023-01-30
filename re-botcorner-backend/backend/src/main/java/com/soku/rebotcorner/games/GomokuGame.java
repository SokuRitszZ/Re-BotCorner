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

public class GomokuGame extends AbsGame {
  private static int[] dx = {-1, -1, -1, 0, 1, 1, 1, 0};
  private static int[] dy = {-1, 0, 1, 1, 1, 0, -1, -1};

  private int rows = 15, cols = 15;
  private int[][] g = new int[rows][cols];
  private int[] rc = new int[]{66, 66};
  private AtomicInteger ok = new AtomicInteger();
  private int cur = 0;
  private int step = 0;

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public GomokuGame(String mode, GameMatch match, List<RunningBot> bots) {
    super(mode, match, bots);

    setGameId(5);

    // init g
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
    data.append(rows).append(" ").append(cols).append(" ");
    StringBuilder g = new StringBuilder();
    for (int i = 0; i < rows; i++)
      for (int j = 0; j < cols; j++)
        g.append(this.g[i][j]).append(" ");
    data.append(g).append(" ").append(rc[0]).append(" ").append(rc[1]);
    return data.toString().trim();
  }

  @Override
  protected void _setStep(JSONObject json) {
    Integer id = json.getInt("id");
    Integer r = json.getInt("r");
    Integer c = json.getInt("c");
    if (!beforeSetStep(id, r, c)) return;
    putChess();
    afterSetStep();
  }

  @Override
  public void describeResult() {
    // 有可能是因为中途退出了
    if (getResult() == null || getResult().length() == 0) {
      if (getReason()[0] == null || getReason()[0].length() == 0)
        setResult("黑子不战而胜");
      else
        setResult("白子不战而胜");
    }
    getRecord().set("result", getResult());
  }

  @Override
  public Integer getPlayerCount() {
    return 2;
  }

  @Override
  void _saveScores() {
    int total = 0;
    for (int i = 0; i < getReason().length; i++) {
      String s = getReason()[i];
      if (s == null) continue;
      switch (s) {
        case "战败":
          total += 5;
          setScore(i, -5);
          break;
        case "平局":
          setScore(i, -5);
        default:
          setScore(i, -10);
          break;
      }
    }
    for (int i = 0; i < 2; i++) {
      if (getScores()[i] == 0) setScore(i, total);
    }
  }

  private boolean checkOver() {
    return rc[0] == -1 || rc[0] == 99;
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
            .set("r", -1)
            .set("c", -1)
        );
      } else {
        String[] rc = input.split(" ");
        int r = Integer.parseInt(rc[0]);
        int c = Integer.parseInt(rc[1]);
        _setStep(
          new JSONObject()
            .set("id", cur)
            .set("r", r)
            .set("c", c)
        );
      }
    }
  }

  private boolean beforeSetStep(int id, int r, int c) {
    rc[0] = r;
    rc[1] = c;
    return checkValid(id, r, c);
  }

  private void putChess() {
    if (!beforePutChess()) return;

    g[rc[0]][rc[1]] = cur;

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

  private boolean checkValid(int id, int r, int c) {
    if (id != cur) return false;
    if (r == -1) return true;
    return g[r][c] == 2;
  }

  private boolean beforePutChess() {
    return !checkOver();
  }

  private void afterPutChess() {
    addStep_put();

    if (!checkLink(rc[0], rc[1]) && step + 1 != rows * cols) return;
    // 游戏结束
    StringBuilder result = new StringBuilder();
    if (step + 1 != rows * cols) {
      result.append(cur == 0 ? "黑子" : "白子").append("获胜");
      setReason(cur ^ 1, "战败");
    } else {
      result.append("平局");
      setReason(0, "平局");
      setReason(1, "平局");
    }
    setResult(result.toString());

    rc[0] = 99;
  }

  private boolean isIn(int x, int y) {
    return 0 <= x && x < 15 && 0 <= y && y < 15;
  }

  private void addStep_put() {
    StringBuilder step = new StringBuilder();
    step.append(transform(rc[0], 36)).append(transform(rc[1], 36));
    getMatch().broadCast(
      new JSONObject()
        .set("action", "set step truly")
        .set("data",
          new JSONObject()
            .set("step", step.toString()))
    );
    addStep(step.toString());
  }

  private boolean checkLink(int r, int c) {
    for (int i = 0; i < 8; i++) {
      int or = r, oc = c;
      int nr = or, nc = oc;
      while (isIn(nr, nc) && g[nr][nc] == g[or][oc]) {
        nr -= dx[i];
        nc -= dy[i];
      }
      int cnt = 0;
      nr += dx[i];
      nc += dy[i];
      while (isIn(nr, nc) && g[nr][nc] == g[or][oc]) {
        ++cnt;
        nr += dx[i];
        nc += dy[i];
      }
      if (cnt >= 5) return true;
    }
    return false;
  }
}
