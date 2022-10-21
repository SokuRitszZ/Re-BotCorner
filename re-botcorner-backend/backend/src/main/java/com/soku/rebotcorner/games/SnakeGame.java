package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.Res;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class SnakeGame extends AbsGame {
  private static final int[] dx = { -1, 0, 1, 0 };
  private static final int[] dy = { 0, 1, 0, -1 };

  private int rows;
  private int cols;
  private int innerWallsCount;
  private Deque<Pair>[] snakes;
  private int[][] g;
  private int[][] initG;
  private int[] directions;
  private String[] state;
  private ReentrantLock lock;

  private int step;

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public SnakeGame(
    String mode,
    GameMatch match,
    List<RunningBot> bots
  ) {
    super(mode, match, bots);

    this.setGameId(1);

    // init grid
    this.rows = 12;
    this.cols = 13;
    this.innerWallsCount = 20;
    createMap();
    this.getRecord().set("initG", this.initG);

    // init snake
    this.snakes = new Deque[2];
    this.snakes[0] = new LinkedList<>();
    this.snakes[1] = new LinkedList<>();
    this.snakes[0].addFirst(new Pair(this.rows - 2, 1));
    this.snakes[1].addFirst(new Pair(1, this.cols - 2));
    this.state = new String[2];

    // init operations
    this.lock = new ReentrantLock();
    this.directions = new int[2];
    directions[0] = directions[1] = -1;
  }

  /**
   * 获取玩家数
   *
   * @return
   */
  @Override
  public Integer getPlayerCount() {
    return 2;
  }

  /**
   * 获取初始数据
   *
   * @return
   */
  @Override
  public JSONObject getInitDataAndSave() {
    // 行、列、地图
    JSONObject json = new JSONObject();
    json.set("rows", this.rows);
    json.set("cols", this.cols);
    json.set("initG", this.initG);
    this.getRecord().set("initData", json);
    return json;
  }

  /**
   * 从前端接收信息并处理
   *
   * @param json
   */
  @Override
  public void setStep(JSONObject json) {
    if (!isHasStart()) return ;
    Integer id = json.getInt("id");
    Integer direction = json.getInt("direction");
    this.setDirectionAndCheckMove(id, direction);
    JSONObject returnJson = new JSONObject();
    returnJson.set("action", "setStep");
    returnJson.set("step", json);
    this.getMatch().broadCast(Res.ok(returnJson));
  }

  /**
   * 移动蛇
   */
  private void moveSnake() {
    boolean isIncreasing = this.checkIncreasing();
    ++step;
    for (int i = 0; i < 2; ++i) {
      this.state[i] = "move";
      Pair first = snakes[i].getFirst();
      if (!isIncreasing) {
        Pair last = snakes[i].getLast();
        this.g[last.x][last.y] -= 1;
        this.snakes[i].removeLast();
      }
      int d = this.directions[i];
      int nx = first.x + dx[d];
      int ny = first.y + dy[d];
      Pair newFirst = new Pair(nx, ny);
      this.g[nx][ny] += 1;
      snakes[i].addFirst(newFirst);
      if (this.g[nx][ny] >= 2) this.setReason(i, "正常战败");
    }

    // 保存在steps
    this.getSteps().append("" + this.directions[0] + this.directions[1]);

    // 发送信息
    JSONObject json = new JSONObject();
    json.set("action", "moveSnake");
    json.set("directions", this.directions);
    json.set("isIncreasing", isIncreasing);
    json.set("state", this.state);
    this.getMatch().broadCast(Res.ok(json));

    // 清空保存的操作
    this.directions[0] = this.directions[1] = -1;
  }

  /**
   * 检测是否成长
   *
   * @return
   */
  private boolean checkIncreasing() {
    return this.step < 10 || (this.step - 9) % 3 == 0;
  }

  /**
   * 巡回
   */
  private void nextStep() {
    if (this.checkOver()) {
      this.gameOver();
      return ;
    }
    runBot();
    if (this.checkOver()) this.gameOver();
  }

  /**
   * 检测是否结束
   *
   * @return
   */
  private boolean checkOver() {
    for (String s : this.state)
      if (s != null && s.equals("die"))
        return true;
    return false;
  }

  /**
   * 运行Bot
   */
  private void runBot() {
    AtomicInteger runOk = new AtomicInteger();
    List<RunningBot> bots = this.getBots();
    for (int i = 0; i < bots.size(); ++i) {
      RunningBot bot = bots.get(i);
      int fi = i;
      if (bot != null)
        new Thread(() -> {
          bot.prepareData(fi + " " + this.parseDataString());
          JSONObject json = JSONUtil.parseObj(bot.run());
          String result = json.getStr("data");
          String check = this.checkResult(result);
          // 检查结果是否合法
          if (!check.equals(result)) {
            this.setReason(fi, check);
          } else {
            json = new JSONObject();
            json.set("action", "setStep");
            json.set("id", fi);
            json.set("direction", Integer.parseInt(result));
            this.setStep(json);
          }
          runOk.incrementAndGet();
        }).start();
      else runOk.incrementAndGet();
    }
    while (runOk.get() != 2) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * 检查输出是否合法
   *
   * @param result
   * @return
   */
  private String checkResult(String result) {
    try {
      if (result == null || result.length() == 0) throw new RuntimeException("输出为空，或者运行超时");
      Pattern pattern = Pattern.compile("^[-\\+]?\\d+$");
      if (!pattern.matcher(result).matches()) throw new RuntimeException(String.format("非法输出：%s", result));
      int d = Integer.parseInt(result);
      if (d < 0 || d > 3) throw new RuntimeException(String.format("非法输出: %d", d));
    } catch (Exception e) {
      return e.getMessage();
    }
    return result;
  }

  /**
   * 将结果保存在录像中
   */
  @Override
  public void setResultToRecord() {
    String result = "";
    if (isDie(0) && isDie(1)) result = "平局";
    else if (isDie(0)) result = "红蛇胜利";
    else result = "蓝蛇胜利";
    this.setResult(result);
    this.getRecord().set("result", result);
  }

  /**
   * 检测是否死亡
   *
   * @param id
   * @return
   */
  boolean isDie(Integer id) {
    return "die".equals(this.state[id]);
  }

  /**
   * 设置失败原因
   *
   * @param id
   * @param reason
   */
  public void setReason(Integer id, String reason) {
    this.state[id] = "die";
    super.setReason(id, reason);
  }

  /**
   * 设置方向并检测是否可以移动蛇
   *
   * @param id
   * @param direction
   */
  private void setDirectionAndCheckMove(Integer id, Integer direction) {
    lock.lock();
    this.directions[id] = direction;
    lock.unlock();
    new Thread(() -> {
      if (!this.checkDirections()) return ;
      if (this.checkOver()) {
        this.gameOver();
        return ;
      }

      // 两个都准备好了，开始移动
      this.moveSnake();

      // 下一步
      this.nextStep();
    }).start();
  }

  /**
   * 检测所有人是否就绪
   *
   * @return
   */
  private boolean checkDirections() {
    lock.lock();
    boolean ok = this.directions[0] != -1 && this.directions[1] != -1;
    lock.unlock();
    return ok;
  }

  /**
   * 游戏开始
   */
  @Override
  public void start() {
    this.setHasStart(true);
    this.g[this.rows - 2][1] = this.g[1][this.cols - 2] = 1;
    this.nextStep();
  }

  /**
   * 将信息序列化
   *
   * @return
   */
  @Override
  public String parseDataString() {
    StringBuilder map = new StringBuilder();

    for (int i = 0; i < this.rows; ++i)
      for (int j = 0; j < this.cols; ++j)
        map.append(g[i][j]);

    StringBuilder data = new StringBuilder();
    data.append(String.valueOf(rows) + " ");
    data.append(String.valueOf(cols) + " ");
    data.append(String.valueOf(step) + " ");
    data.append(map + " ");
    data.append(String.valueOf(snakes[0].size()) + " ");
    data.append(String.valueOf(snakes[1].size()) + " ");
    for (Deque<Pair> snake : this.snakes) {
      StringBuilder snakeStr = new StringBuilder();
      for (Pair pair : snake) {
        snakeStr.append(String.valueOf(pair.x) + " ");
        snakeStr.append(String.valueOf(pair.y) + " ");
      }
      data.append(snakeStr);
    }
    return data.toString();
  }

  /**
   * 造图
   */
  void createMap() {
    this.g = new int[this.rows][this.cols];
    for (int i = 0; i < 1000; ++i) if (this.draw()) break;
    this.initG = new int[this.rows][this.cols];
    for (int i = 0; i < this.rows; ++i)
      for (int j = 0; j < this.cols; ++j)
        this.initG[i][j] = this.g[i][j];
  }

  /**
   * 造一个墙
   *
   * @return
   */
  boolean draw() {
    for (int i = 0; i < this.rows; ++i) for (int j = 0; j < this.cols; ++j) g[i][j] = 0;
    for (int i = 0; i < this.rows; ++i) g[i][0] = g[i][this.cols - 1] = 1;
    for (int i = 0; i < this.cols; ++i) g[0][i] = g[this.rows - 1][i] = 1;

    Random random = new Random();
    for (int i = 0; i < this.innerWallsCount / 2; ++i) for (int j = 0; j < 1000; ++j) {
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
    return checkValid(this.rows - 2, 1, 1, this.cols - 2);
  }

  /**
   * 检测连通性
   *
   * @param sx
   * @param sy
   * @param tx
   * @param ty
   * @return
   */
  boolean checkValid(int sx, int sy, int tx, int ty) {
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

  /**
   * 输出棋盘（测试用）
   */
  private void display() {
    for (int i = 0; i < this.rows; ++i) {
      for (int j = 0; j < this.cols; ++j)
        System.out.print(this.g[i][j] + " ");
      System.out.println();
    }
  }

   /**
   * 输出初始棋盘（测试用）
   */
  private void displayInitG() {
    for (int i = 0; i < this.rows; ++i) {
      for (int j = 0; j < this.cols; ++j)
        System.out.print(this.initG[i][j] + " ");
      System.out.println();
    }
  }
}
