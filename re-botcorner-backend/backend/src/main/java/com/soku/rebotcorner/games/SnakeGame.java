package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;

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

  private Vector<Integer> directions = new Vector<>(2);
  private String[] state;
  private ReentrantLock lock;
  private AtomicInteger ok = new AtomicInteger();
  private int step;
  private UUID uuid = UUID.randomUUID();

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

    setGameId(1);

    // init grid
    rows = 12;
    cols = 13;
    innerWallsCount = 20;
    createMap();

    // init snake
    snakes = new Deque[2];
    snakes[0] = new LinkedList<>();
    snakes[1] = new LinkedList<>();
    snakes[0].addFirst(new Pair(1, cols - 2));
    snakes[1].addFirst(new Pair(rows - 2, 1));
    state = new String[2];

    // init operations
    lock = new ReentrantLock();
    directions.add(-1);
    directions.add(-1);
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
    getMatch().broadCast(
      new JSONObject()
        .set("action", "set step")
        .set("data", json)
    );
    return true;
  }

  public void afterSetStep(JSONObject json) {
    // 检测是否已经设置好所有方向了
    if (!checkDirections()) return ;

    // 两个都准备好了，开始移动
    moveSnake();

    if (checkOver()) {
      gameOver();
    }
  }

  /**
   * 从前端接收信息并处理
   *
   * @param json
   */
  @Override
  protected void _setStep(JSONObject json) {
    Integer id = json.getInt("id");
    Integer d = json.getInt("d");

    if (beforeSetStep(json)) {
      setDirection(id, d);
      afterSetStep(json);
    }
  }

  private boolean beforeMoveSnake() {
    // 处理Bot输出数据错误导致的游戏结束
    if (checkOver()) return false;
    return true;
  }

  private void afterMoveSnake() {
    // 处理正常的战败
    boolean isIncreasing = checkIncreasing();

    // 检测当前状态
    for (int i = 0; i < snakes.length; i++) {
      Pair first = snakes[i].getFirst();
      if (g[first.x][first.y] > 1) {
        state[i] = "die";
        setReason(i, "碰撞致死");
      }
    }

    // 发送移动蛇的信息
    String step = ""
      + directions.get(0) + directions.get(1)
      + (isIncreasing ? 1 : 0)
      + (state[0] == "die" ? 1 : 0) + (state[1] == "die" ? 1 : 0);

    // 保存在steps
    getSteps().append(step);

    // 发送信息
    getMatch()
      .broadCast(
        new JSONObject()
          .set("action", "set step truly")
          .set("data", new JSONObject()
            .set("step", step)
          )
      );

    // 处理现场
    directions.set(0, -1);
    directions.set(1, -1);
    ++this.step;

    ok.getAndIncrement();
  }

  /**
   * 移动蛇
   */
  private void moveSnake() {
    if (!beforeMoveSnake()) return ;

    boolean isIncreasing = checkIncreasing();
    // 移动
    for (int i = 0; i < 2; ++i) {
      state[i] = "alive";
      if (!isIncreasing) {
        Pair last = snakes[i].getLast();
        g[last.x][last.y] -= 1;
        snakes[i].removeLast();
      }
    }
    for (int i = 0; i < 2; ++i) {
      Pair first = snakes[i].getFirst();
      int d = directions.get(i);
      int nx = first.x + dx[d];
      int ny = first.y + dy[d];
      Pair newFirst = new Pair(nx, ny);
      g[nx][ny] += 1;
      snakes[i].addFirst(newFirst);
    }

    afterMoveSnake();
  }

  /**
   * 检测是否成长
   *
   * @return
   */
  private boolean checkIncreasing() {
    return step < 10 || (step - 9) % 3 == 0;
  }

  /**
   * 巡回
   */
  private void nextStep() {
    runBot();
  }

  /**
   * 检测是否结束
   *
   * @return
   */
  private boolean checkOver() {
    for (int i = 0; i < 2; i++)
      if (directions.get(i) == 4)
        state[i] = "die";
    for (String s : state)
      if (s != null && s.equals("die"))
        return true;
    return false;
  }

  /**
   * 运行Bot
   */
  private void runBot() {
    List<RunningBot> bots = getBots();
    for (int i = 0; i < bots.size(); ++i) {
      RunningBot bot = bots.get(i);
      int fi = i;
      if (bot != null)
        new Thread(() -> {
          bot.prepareData(fi + " " + parseDataString());
          JSONObject json = JSONUtil.parseObj(bot.run());
          String result = json.getStr("data");
          String check = checkResult(result);
          // 检查结果是否合法
          if (!check.equals(result)) {
            state[fi] = "die";
            setReason(fi, check);
            _setStep(
              new JSONObject()
                .set("id", fi)
                .set("d", 4)
            );
          } else {
            _setStep(
              new JSONObject()
                .set("id", fi)
                .set("d", Integer.parseInt(result))
            );
          }
        }).start();
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
  public void describeResult() {
    String result = "";
    if (isDie(0) && isDie(1)) result = "平局";
    else if (isDie(0)) result = "蓝蛇胜利";
    else result = "红蛇胜利";
    setResult(result);
  }

  /*u
   * 检测是否死亡
   *
   * @param id
   * @return
   */
  boolean isDie(Integer id) {
    return "die".equals(state[id]);
  }

  /**
   * 设置方向并检测是否可以移动蛇
   *
   * @param id
   * @param direction
   */
  private void setDirection(Integer id, Integer direction) {
    directions.set(id, direction);
  }

  /**
   * 检测所有人是否就绪
   *
   * @return
   */
  private boolean checkDirections() {
    boolean ok = directions.get(0) != -1 && directions.get(1) != -1;
    return ok;
  }

  /**
   * 游戏开始
   */
  @Override
  public void start() {
    setHasStart(true);
    g[rows - 2][1] = g[1][cols - 2] = 1;
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
   * 将信息序列化
   *
   * @return
   */
  @Override
  public String parseDataString() {
    StringBuilder map = new StringBuilder();

    for (int i = 0; i < rows; ++i)
      for (int j = 0; j < cols; ++j)
        map.append(g[i][j]);

    StringBuilder data = new StringBuilder();
    data.append(rows + " ");
    data.append(cols + " ");
    data.append(step + " ");
    data.append(map + " ");
    data.append(snakes[0].size() + " ");
    data.append(snakes[1].size() + " ");
    for (Deque<Pair> snake : snakes) {
      StringBuilder snakeStr = new StringBuilder();
      for (Pair pair : snake) {
        snakeStr.append(pair.x + " ");
        snakeStr.append(pair.y + " ");
      }
      data.append(snakeStr);
    }
    return data.toString();
  }

  /**
   * 造图
   */
  void createMap() {
    g = new int[rows][cols];
    for (int i = 0; i < 1000; ++i) if (draw()) break;
  }

  /**
   * 造一个墙
   *
   * @return
   */
  boolean draw() {
    for (int i = 0; i < rows; ++i) for (int j = 0; j < cols; ++j) g[i][j] = 0;
    for (int i = 0; i < rows; ++i) g[i][0] = g[i][cols - 1] = 1;
    for (int i = 0; i < cols; ++i) g[0][i] = g[rows - 1][i] = 1;

    Random random = new Random();
    for (int i = 0; i < innerWallsCount / 2; ++i) for (int j = 0; j < 1000; ++j) {
      int r = random.nextInt(rows);
      int c = random.nextInt(cols);
      if (g[r][c] == 1 || g[rows - 1 - r][cols - 1 - c] == 1) {
        continue;
      }
      if ((r == rows - 2 && c == 1) || (r == 1 && c == cols - 2)) {
        continue;
      }
      g[r][c] = g[rows - 1 - r][cols - 1 - c] = 1;
      break;
    }
    return checkValid(rows - 2, 1, 1, cols - 2);
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
    boolean[][] st = new boolean[rows][cols];
    st[sx][sy] = true;
    while (!q.isEmpty()) {
      Pair u = q.remove();
      for (int i = 0; i < 4; ++i) {
        int nx = u.x + dx[i];
        int ny = u.y + dy[i];
        if (nx < 0 || nx >= rows || ny < 0 || ny >= cols) {
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
    System.out.println(uuid.toString());
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j)
        System.out.print(g[i][j] + " ");
      System.out.println();
    }
    System.out.println();
  }
}
