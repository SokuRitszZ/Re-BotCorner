package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.Res;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ReversiGame extends AbsGame {
  private static int dx[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
  private static int dy[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

  private final int rows;
  private final int cols;
  private int[] rc = new int[2];
  private int[][] g;
  private int step;
  private AtomicInteger ok = new AtomicInteger();

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public ReversiGame(
    String mode,
    GameMatch match,
    List<RunningBot> bots
  ) {
    super(mode, match, bots);

    setGameId(2);

    // init g
    rows = cols = 8;
    g = new int[rows][cols];
    step = 0;
    for (int i = 0; i < rows; ++i)
      for (int j = 0; j < cols; ++j)
        g[i][j] = 2;
    int mdRow = rows >> 1;
    int mdCol = cols >> 1;
    g[mdRow - 1][mdCol - 1] = g[mdRow][mdCol] = 1;
    g[mdRow][mdCol - 1] = g[mdRow - 1][mdCol] = 0;
  }

  /**
   * 玩家人数
   *
   * @return
   */
  @Override
  public Integer getPlayerCount() {
    return 2;
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

  public boolean beforeSetStep(JSONObject json) {
    Integer id = json.getInt("id");
    Integer r = json.getInt("r");
    Integer c = json.getInt("c");
    this.rc[0] = r;
    this.rc[1] = c;
    return checkValid(id, r, c);
  }

  private void afterSetStep(JSONObject json) {
    if (checkOver()) {
      gameOver();
    }
    if (checkPass()) {
      _setStep(
        new JSONObject()
          .set("id", step % 2)
          .set("r", 9)
          .set("c", 9)
      );
    }
  }

  /**
   * 从前端接收并处理步
   *
   * @param json
   */
  @Override
  protected void _setStep(JSONObject json) {
    Integer id = json.getInt("id");
    Integer r = json.getInt("r");
    Integer c = json.getInt("c");

    if (beforeSetStep(json)) {
      putChess(id, r, c);
      afterSetStep(json);
    }
  }

  private boolean beforePutChess() {
    // 处理Bot输出数据导致的游戏结束
    return !checkOver();
  }

  private void afterPutChess() {
    // 发送信息
    String step = "" + this.step % 2 + this.rc[0] + this.rc[1];
    getMatch()
      .broadCast(
        new JSONObject()
          .set("action", "set step truly")
          .set("data",
            new JSONObject()
              .set("step", step)
          )
      );
    getSteps().append(step);

    // 处理正常输
    int[] cnt = new int[2];
    // 已经没有某个颜色的棋子
    for (int[] ints : g) {
      for (int anInt : ints) {
        if (anInt != 2)
          ++cnt[anInt];
      }
    }
    int id = cnt[0] == 0 ? 0 : 1;
    if (cnt[id] == 0) {
      this.setResult((id == 0 ? "白子" : "黑子") + "胜利");
      this.setReason(id, (id == 0 ? "黑子" : "白子") + "失去所有棋子");
      rc[0] = -1;
      return ;
    }
    // 已经放满了棋盘
    if (cnt[0] + cnt[1] == rows * cols) {
      int i = cnt[0] > cnt[1] ? 0 : 1;
      if (cnt[i] == cnt[i ^ 1]) {
        setResult("平局");
        for (int i1 = 0; i1 < 2; i1++) {
          this.setReason(i1, "棋子数相等");
        }
      } else {
        setResult((i == 0 ? "黑子" : "白子") + "胜利");
        setReason(i ^ 1, "棋数更少");
      }
      rc[0] = -1;
      return ;
    }

    ++this.step;
    rc[0] = 8; // 还能继续
    ok.getAndIncrement();
  }


  /**
   * 落子
   *
   * @param id
   * @param r
   * @param c
   */
  private void putChess(int id, int r, int c) {
    if (!beforePutChess()) return ;

    rc[0] = r;
    rc[1] = c;

    if (r != 9) {
      g[r][c] = id;
      for (int i = 0; i < 8; ++i) {
        if (isValidDir(r, c, i)) {
          int nr = r + dx[i];
          int nc = c + dy[i];
          while (isIn(nr, nc) && g[nr][nc] == (id ^ 1)) {
            g[nr][nc] = id;
            nr += dx[i];
            nc += dy[i];
          }
        }
      }
    }

    afterPutChess();
  }

  private boolean checkOver() {
    if (rc[0] != -1) return false;
    return true;
  }

  /**
   * 输出测试
   */
  private void display() {
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        System.out.print(g[i][j] + " ");
      }
      System.out.println();
    }
  }

  /**
   * 游戏开始
   */
  @Override
  public void start() {
    setHasStart(true);

    ok.getAndIncrement();
    new Thread(() -> {
      while (!checkOver()) {
        if (ok.get() > 0) {
          ok.decrementAndGet();
          nextStep();
        }
      }
    }).start();
  }

  /**
   * 下一步
   */
  private void nextStep() {
    runBot();
  }

  /**
   * 运行Bot
   */
  private void runBot() {
    int id = step % 2;
    if (getBots().get(id) == null) return ;
    String data = id + " " + parseDataString();
    RunningBot bot = getBots().get(id);
    bot.prepareData(data);
    JSONObject json = JSONUtil.parseObj(bot.run());
    String result = json.getStr("data");
    try {
      if (result == null || result.trim().length() == 0)
        throw new RuntimeException("运行超时，或者输出为空");
      result = result.trim();
      Pattern pattern = Pattern.compile("^[0-7]\\s[0-7]$");
      if (!pattern.matcher(result).matches())
        throw new RuntimeException(String.format("非法输出: %s", result));
      String[] rc = result.split(" ");
      int r = Integer.parseInt(rc[0]);
      int c = Integer.parseInt(rc[1]);
      if (!checkValid(id, r, c))
        throw new RuntimeException(String.format("非法操作: %s", result));
      _setStep(
        new JSONObject()
          .set("id", id)
          .set("r", r)
          .set("c", c)
      );
    } catch (Exception e) {
      String reason = e.getMessage();
      setReason(id, reason);
      _setStep(
        new JSONObject()
          .set("id", id)
          .set("r", -1)
          .set("c", -1)
      );
    }
  }

  /**
   * 检测是否跳过
   *
   * @return
   */
  private boolean checkPass() {
    boolean isHasEmpty = false;
    for (int i = 0; i < rows; ++i)
      for (int j = 0; j < cols; ++j)
        if (checkValid(step % 2, i, j)) {
          return false;
        } else if (g[i][j] == 2) {
          isHasEmpty = true;
        }
    return isHasEmpty;
  }

  /**
   * 检测是否合法
   *
   * @param id
   * @param r
   * @param c
   * @return
   */
  private boolean checkValid(int id, int r, int c) {
    if (id != step % 2) return false;
    if (r == 9) return true;
    if (!isIn(r, c) || g[r][c] != 2)
      return false;
    for (int i = 0; i < 8; ++i)
      if (isValidDir(r, c, i))
        return true;
    return false;
  }

  /**
   * 检测方向是否可行
   *
   * @param r
   * @param c
   * @param i
   * @return
   */
  private boolean isValidDir(int r, int c, int i) {
    int id = step % 2;
    int nr = r + dx[i];
    int nc = c + dy[i];
    boolean flg = false;
    while (isIn(nr, nc) && g[nr][nc] == (id ^ 1)) {
      nr += dx[i];
      nc += dy[i];
      flg = true;
    }
    return flg && isIn(nr, nc) && g[nr][nc] == id;
  }

  /**
   * 检测是否在棋盘内
   *
   * @param r
   * @param c
   * @return
   */
  private boolean isIn(int r, int c) {
    return r >= 0 && c >= 0 && r < rows && c < cols;
  }

  /**
   * 获取序列化数据
   *
   * @return
   */
  @Override
  public String parseDataString() {
    StringBuilder data = new StringBuilder("");
    data.append(rows);
    data.append(" ").append(cols);
    String stringifiedChess;
    stringifiedChess = "";
    for (int i = 0; i < rows; ++i)
      for (int j = 0; j < cols; ++j)
        stringifiedChess += g[i][j];
    data.append(" ").append(stringifiedChess);
    return data.toString();
  }

  /**
   * 保存结果给录像
   */
  @Override
  public void describeResult() {
    if (getResult() == null || getResult().length() == 0) {
      if (getReason()[0] == null || getReason()[0].length() == 0)
        setResult("黑获胜");
      else
        setResult("白获胜");
    }
    getRecord().set("result", getResult());
  }
}
