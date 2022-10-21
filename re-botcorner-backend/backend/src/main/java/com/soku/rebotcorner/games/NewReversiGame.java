package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.Res;

import java.util.List;
import java.util.regex.Pattern;

public class NewReversiGame extends AbsGame {
  private static int dx[] = { -1, -1, 0, 1, 1, 1, 0, -1 };
  private static int dy[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

  private final int rows;
  private final int cols;
  private int[][] chess;
  private int step;

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public NewReversiGame(
    String mode,
    GameMatch match,
    List<RunningBot> bots
  ) {
    super(mode, match, bots);

    this.setGameId(2);

    // init chess
    this.rows = this.cols = 8;
    this.chess = new int[rows][cols];
    this.step = 0;
    for (int i = 0; i < rows; ++i)
      for (int j = 0; j < cols; ++j)
        this.chess[i][j] = 2;
    int mdRow = rows >> 1;
    int mdCol = cols >> 1;
    chess[mdRow - 1][mdCol - 1] = chess[mdRow][mdCol] = 1;
    chess[mdRow][mdCol - 1] = chess[mdRow - 1][mdCol] = 0;
  }

  /**
   * 玩家人数
   *
   * @return
   */
  @Override
  Integer getPlayerCount() {
    return 2;
  }

  /**
   * 获取初始值并且保存起来
   *
   * @return
   */
  @Override
  public JSONObject getInitDataAndSave() {
    JSONObject json = new JSONObject();
    json.set("initChess", this.chess);
    this.getRecord().set("initData", json);
    return json;
  }

  /**
   * 从前端接收并处理步
   *
   * @param json
   */
  @Override
  public void setStep(JSONObject json) {
    if (!this.isHasStart() || this.isHasOver()) return ;
//    this.display();
    Integer id = json.getInt("id");
    Integer r = json.getInt("r");
    Integer c = json.getInt("c");
    this.checkAndPutChess(id, r, c);
  }

  private void display() {
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        System.out.print(chess[i][j] + " ");
      }
      System.out.println();
    }
  }

  /**
   * 检测并落子
   *
   * @param id
   * @param r
   * @param c
   */
  private void checkAndPutChess(Integer id, Integer r, Integer c) {
    if (!checkValid(id, r, c)) {
      this.getMatch().sendOne(id, Res.fail("操作不合法，请重试"));
      return ;
    }
    this.putChess(id, r, c);
  }

  /**
   * 游戏开始
   */
  @Override
  public void start() {
    this.setHasStart(true);
    this.nextStep();
  }

  /**
   * 下一步
   */
  private void nextStep() {
    if (this.checkOverAndSaveResult()) {
      this.gameOver();
      return ;
    }
    // 检测是否跳过
    if (this.checkPass()) this.pass();
    this.runBot();
  }

  /**
   * 运行Bot
   */
  private void runBot() {
    int id = step % 2;
    if (this.getBots().get(id) == null) return ;
    String data = id + " " + parseDataString();
    RunningBot bot = this.getBots().get(id);
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
      this.putChess(id, r, c);
    } catch (Exception e) {
      String reason = e.getMessage();
      this.setReason(id, reason);
      this.gameOver();
    }
  }

  /**
   * 落子
   *
   * @param id
   * @param r
   * @param c
   */
  private void putChess(int id, int r, int c) {
    this.chess[r][c] = id;
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
    ++this.step;

    JSONObject json = new JSONObject();
    json.set("action", "putChess");
    json.set("id", id);
    json.set("r", r);
    json.set("c", c);
    this.getMatch().broadCast(Res.ok(json));

    // 保存到录像
    getSteps().append(r + "" + c);

    // 下一步
    new Thread(() -> {
      this.nextStep();
    }).start();
  }

  /**
   * 跳过
   */
  private void pass() {
    ++this.step;
    JSONObject json = new JSONObject();
    json.set("action", "pass");
    json.set("passTo", step % 2);
    this.getMatch().broadCast(Res.ok(json));

    // 保存到录像
    getSteps().append("ps");
  }

  /**
   * 检测是否跳过
   *
   * @return
   */
  private boolean checkPass() {
    for (int i = 0; i < rows; ++i)
      for (int j = 0; j < cols; ++j)
        if (checkValid(step % 2, i, j))
          return false;
    return true;
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
    if (!isIn(r, c) || chess[r][c] != 2)
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
    while (isIn(nr, nc) && chess[nr][nc] == (id ^ 1)) {
      nr += dx[i];
      nc += dy[i];
      flg = true;
    }
    return flg && isIn(nr, nc) && chess[nr][nc] == id;
  }

  /**
   * 检测是否在棋盘内
   *
   * @param r
   * @param c
   * @return
   */
  private boolean isIn(int r, int c) {
    return r >= 0 && c >= 0 && r < this.rows && c < this.cols;
  }

  /**
   * 检测是否结束并保存结果
   *
   * @return
   */
  private boolean checkOverAndSaveResult() {
    int id = step % 2;
    boolean flag = true;
    for (int[] ints : chess)
      for (int anInt : ints)
        if (anInt == id) {
          flag = false;
          break;
        }
    if (flag) {
      this.setReason(id, "战败");
      this.setReason(id ^ 1, "获胜");
      this.setResult(id == 0 ? "白获胜" : "黑获胜");
      return true;
    }
    int[] cnt = new int[2];
    for (int i = 0; i < rows; i++)
      for (int j = 0; j < cols; j++) {
        if (chess[i][j] == 2)
          return false;
        ++cnt[chess[i][j]];
      }
    if (cnt[0] == cnt[1]) {
      this.setReason(0, "平局");
      this.setReason(1, "平局");
      this.setResult("平局");
    } else if (cnt[0] > cnt[1]) {
      this.setReason(0, "获胜");
      this.setReason(1, "战败");
      this.setResult("黑获胜");
    } else {
      this.setReason(1, "获胜");
      this.setReason(0, "战败");
      this.setResult("白获胜");
    }
    return true;
  }

  /**
   * 获取序列化数据
   *
   * @return
   */
  @Override
  String parseDataString() {
    StringBuilder data = new StringBuilder("");
    data.append(rows);
    data.append(" ").append(cols);
    String stringifiedChess;
    stringifiedChess = "";
    for (int i = 0; i < rows; ++i)
      for (int j = 0; j < cols; ++j)
        stringifiedChess += chess[i][j];
    data.append(" ").append(stringifiedChess);
    return data.toString();
  }

  /**
   * 保存结果给录像
   */
  @Override
  protected void setResultToRecord() {
    if (this.getResult() == null || this.getResult().length() == 0) {
      if (this.getReason()[0] == null || this.getReason()[0].length() == 0)
        this.setResult("黑获胜");
      else
        this.setResult("白获胜");
    }
    this.getRecord().set("result", this.getResult());
  }
}
