package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.soku.rebotcorner.utils.ScaleUtil.transform;

/**
 * 西洋双路棋
 * <p>
 * 规定：
 * 1. 0是白方的老家，25是红方的老家
 * 2. 1-24都是棋盘上的正常的位置
 * 3. 白方的本区是1-6，红方的本区是17-24
 * 4. 白方的终点是26，红方的终点是27
 * <p>
 * 基本流程：
 * 人手模式：
 * 1. 通过setStep进入入口，在进入之前先验证操作是否合法，放入ft数组，如果合法就继续，否则拦截
 * 2. 通过验证之后进行真正的移动。当然，移动也有关卡：如果操作不合法，那就要在ft给ft[0] = -1，然后不让继续进行。
 * 3. 如果移动的关卡通过了，那么就进行移动，并更新棋盘信息。
 * 4. 移动之后有afterMoveChess，用来处理后续动作：发送信息，并且检查局势，如果正常结束了，就设ft[0] = 99
 * 5. 出来到afterSetStep，检查ft数组是否已经提示已经结束，如果结束了，就gameOver，否则：
 * 1. 检测是否已经用完骰子了，如果用完了，就换手
 * 2. 检测是否跳过，如果要跳过，就一直跳过，直到不用跳过为止
 * <p>
 * 录像文件格式：
 * initData: {
 * mask: 棋盘
 * dice: 初始骰子点数
 * start: 先手者，0为白方1为红方
 * }
 * step分为：
 * 1. v.. 移动
 * 2. z.{2,4} 骰子
 * 3. t 换手
 * 4. p 跳过
 */
public class BackgammonGame extends AbsGame {
  private static final Random random = new Random();

  private final AtomicInteger ok = new AtomicInteger();
  private final int[] ft = new int[2];

  private List<Integer> g[] = new List[28];
  private Vector<Integer> dice = new Vector<>();
  private int cur = 0;

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public BackgammonGame(String mode, GameMatch match, List<RunningBot> bots) {
    super(mode, match, bots);

    setGameId(3);

    // 初始化棋盘
    initG();
//    test_initG();

    // 初始化骰子，决定谁先手
    flushDice();
    while (dice.get(0) == dice.get(1)) flushDice();
    cur = dice.get(0) > dice.get(1) ? 0 : 1;
  }

  @Override
  public void start() {
    flushDice();
    setHasStart(true);
    addStep_dice();

    ok.getAndIncrement();
    new Thread(() -> {
      while (!checkOver()) {
        if (ok.get() > 0) {
          runBot();
          ok.decrementAndGet();
        }
      }
    }).start();
  }

  @Override
  public String parseDataString() {
    StringBuilder data = new StringBuilder(cur + " ");
    for (int i = 0; i < 26; ++i)
      if (g[i].size() == 0) data.append("2 0 ");
      else data.append(g[i].get(0)).append(" ").append(g[i].size()).append(" ");
    data.append(dice.size()).append(" ");
    for (Integer d : dice)
      data.append(d).append(" ");
    return data.toString().trim();
  }

  @Override
  public void describeResult() {
    // 有可能是因为中途退出了
    if (getResult() == null || getResult().length() == 0) {
      if (getReason()[0] == null || getReason()[0].length() == 0)
        setResult("白方不战而胜");
      else
        setResult("红方不战而胜");
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
    for (int i = 0; i < 2; i++) {
      String s = getReason()[i];
      if (s == null) continue;
      switch (s) {
        case "被全胜":
          total += 9;
          setScore(i, -9);
          break;
        case "被完胜":
          total += 6;
          setScore(i, -6);
          break;
        case "被单胜":
          total += 3;
          setScore(i, -3);
          break;
        default:
          setScore(i, -10);
          break;
      }
    }
    for (int i = 0; i < 2; i++) {
      if (getScores()[i] == 0) setScore(i, total);
    }
  }

  @Override
  protected JSONObject makeInitData() {
    StringBuilder mask = new StringBuilder();
    for (List<Integer> integers : g) {
      if (integers.size() == 0) mask.append("20");
      else mask.append(integers.get(0)).append(transform(integers.size(), 36));
    }

    this.setInitData(
      new JSONObject()
        .set("mask", mask.toString())
        .set("dice", diceStr())
        .set("start", cur)
    );

    return getInitData();
  }

  @Override
  protected void _setStep(JSONObject json) {
    Integer id = json.getInt("id");
    Integer from = json.getInt("from");
    Integer to = json.getInt("to");
    if (!beforeSetStep(id, from, to)) return;
    moveChess();
    afterSetStep();
  }

  private void initG() {
    for (int i = g.length - 1; i >= 0; i--)
      g[i] = new ArrayList<>();
    for (int i = 0; i < 2; ++i) {
      g[1].add(0);
      g[24].add(1);
    }
    for (int i = 0; i < 3; ++i) {
      g[8].add(1);
      g[17].add(0);
    }
    for (int i = 0; i < 5; i++) {
      g[6].add(1);
      g[12].add(0);
      g[19].add(0);
      g[13].add(1);
    }
  }

  private void flushDice() {
    dice.clear();
    int p0 = random.nextInt(6) + 1, p1 = random.nextInt(6) + 1;
    dice.add(p0);
    dice.add(p1);
    if (p0 == p1) {
      dice.add(p0);
      dice.add(p0);
    }
  }

  private String diceStr() {
    StringBuilder sb = new StringBuilder();
    for (Integer die : dice) sb.append(die);
    return sb.toString();
  }

  private void addStep_dice() {
    StringBuilder step = new StringBuilder();
    step.append("z").append(diceStr());
    this.getMatch().broadCast(
      new JSONObject()
        .set("action", "set step truly")
        .set("data",
          new JSONObject()
            .set("step", step.toString()))
    );
    getSteps().append(step);
  }

  private boolean checkOver() {
    return ft[0] == -1 || ft[0] == 99;
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
            .set("from", -1)
            .set("to", -1)
        );
      } else {
        String[] rc = input.split(" ");
        int from = Integer.parseInt(rc[0]);
        int to = Integer.parseInt(rc[1]);
        if (to == 25 || to == 0) to = to == 25 ? 26 : 27;
        _setStep(
          new JSONObject()
            .set("id", cur)
            .set("from", from)
            .set("to", to)
        );
      }
    }
  }

  private String checkInput(String input) {
    if (input == null) return "运行超时";
    input = input.trim();
    if (input.length() == 0) return "输出为空";
    Pattern pattern = Pattern.compile("^((\\d)|([1-9]\\d*))\\s((\\d)|([1-9]\\d*))$");
    if (!pattern.matcher(input).matches())
      return String.format("非法输出0: %s", input);
    String[] rc = input.split(" ");
    int from = Integer.parseInt(rc[0]);
    int to = Integer.parseInt(rc[1]);
    if (to == 0 || to == 25) to = to == 25 ? 26 : 27;
    if (from < 0 || from > 27 || to < 0 || to > 27)
      return String.format("非法输出1: %s", input);
    if (!checkValid(cur, from, to))
      return String.format("非法操作: %s", input);
    return "ok";
  }

  private boolean checkValid(int id, int from, int to) {
    if (from == -1) return true; // 非法操作
    if (!isIn(from) || !isIn(to)) return false; // 不在范围内
    if (from == 26 || from == 27) return false; // 不能从终点出来
    if (id == 0 && to == 27 || id == 1 && to == 26) return false; // 走错地方
    if (id != cur) return false; // 不是当前的棋子操作
    if (g[from].size() == 0) return false; // 从没棋子的地方出来
    if (g[from].get(0) != cur) return false;
    // 老家有没有人
    int home = id == 0 ? 0 : 25;
    if (g[home].size() > 0 && from != home) return false; // 老家还有人但不是从老家出来的
    // 普通的移动
    int end = 26 + id;
    int step = to - from;
    if (27 - to <= 1) step = (id == 0 ? 25 : 0) - from;
    if (cur == 1 && step < 0) step *= -1;
//    System.out.println(id + " " + from + " " + to + " " + step + " " + end);
    if (to != end && !hasDice(step)) return false; // 不是走到终点，没有骰子
    if (to != end && g[to].size() > 1 && g[to].get(0) != id) return false; // 不是走向终点，目标地有两人以上的敌人
    // 归位的移动
    if (to == end && checkBQ(id, id == 0 ? 19 : 1) + g[end].size() != 15) return false; // 走向终点，但本区+终点的人没有凑够15人
    if (to == end && !hasDice(step) && !hasMoreDice(step)) return false; // 走向终点，没有合适的骰子提供
    if (to == end && !hasDice(step) && !checkOutest(id, from)) return false; // 走向终点，没有骰子，不是最外层

    return true;
  }

  private boolean isIn(int x) {
    return 0 <= x && x < 28;
  }

  private boolean beforeSetStep(int id, int from, int to) {
    ft[0] = from;
    ft[1] = to;
    return checkValid(id, from, to);
  }

  private void moveChess() {
    if (!beforeMoveChess()) return;

    int from = ft[0], to = ft[1];

    int end = cur == 0 ? 26 : 27;
    if (to == end) {
      int step = (cur == 0 ? 25 : 0) - from;
      if (cur == 1 && step < 0) step *= -1;
      if (hasDice(step)) use(step);
      else useMax();
      g[to].add(cur);
      g[from].remove(0);
    } else {
      int step = to - from;
      if (cur == 1 && step < 0) step *= -1;
      use(step);
      // 吃
      if (g[to].size() == 1 && g[to].get(0) != cur) {
        Integer j = cur ^ 1;
        g[to].remove(j);
        g[j == 0 ? 0 : 25].add(j);
      }
      g[to].add(cur);
      g[from].remove(0);
    }

    afterMoveChess();
  }

  private boolean beforeMoveChess() {
    return !checkOver();
  }

  private void afterMoveChess() {
    // 发送信息
    // 保存动作到录像
    addStep_move();

    // 检测游戏是否正常结束，如果状态就是正常结束的，那就ft[0] = 99，并结算，但交由afterSetStep来执行游戏结束
    int id = g[26].size() == 15 ? 0 : 1;
    int jd = id ^ 1;
    if (g[26 + id].size() != 15) return;
    int jh = jd == 0 ? 0 : 25;
    StringBuilder result = new StringBuilder();
    result.append(id == 0 ? "白方" : "红方");
    int type = 0;
    if (g[26 + jd].size() == 0) {
      if (g[jh].size() > 0 || checkBQ(jd, jd == 0 ? 1 : 19) > 0) {
        result.append("全胜");
        setReason(jd, "被全胜");
      } else {
        result.append("完胜");
        setReason(jd, "被完胜");
      }
    } else {
      result.append("单胜");
      setReason(jd, "被单胜");
    }
    setResult(result.toString());
    ft[0] = 99;
  }

  private void afterSetStep() {
    if (checkOver()) {
      gameOver();
      return;
    }
    if (checkTurn()) turn();
    while (checkPass()) pass();

    ok.incrementAndGet();
  }

  private boolean hasDice(int die) {
    return dice.contains(die);
  }

  private int checkBQ(int id, int st) {
    int cnt = 0;
    for (int i = 0; i < 6; i++) {
      int j = st + i;
      if (g[j].size() > 0 && g[j].get(0) == id) cnt += g[j].size();
    }
    return cnt;
  }

  private boolean hasMoreDice(int step) {
    for (int i = step + 1; i <= 6; ++i)
      if (hasDice(i)) return true;
    return false;
  }

  private boolean checkOutest(int id, int from) {
    if (id == 0) {
      for (int i = 19; i < from; ++i)
        if (g[i].size() > 0 && g[i].get(0) == id) return false;
    } else {
      for (int i = 6; i > from; --i) {
        if (g[i].size() > 0 && g[i].get(0) == id) return false;
      }
    }
    return true;
  }

  private void use(Integer x) {
    dice.remove(x);
  }

  private void useMax() {
    Integer x = 0;
    for (Integer die : dice)
      if (die > x) x = die;
    dice.remove(x);
  }

  private void addStep_move() {
    StringBuilder step = new StringBuilder();
    step.append("v").append(transform(ft[0], 36)).append(transform(ft[1], 36));
    getMatch().broadCast(
      new JSONObject()
        .set("action", "set step truly")
        .set("data",
          new JSONObject()
            .set("step", step.toString()))
    );
    getSteps().append(step);
  }

  private boolean checkTurn() {
    return dice.size() == 0;
  }

  private void turn() {
    // 先换手并发送信息
    cur ^= 1;
    addStep_turn();

    // 然后刷新骰子
    flushDice();

    // 发送骰子信息
    addStep_dice();
  }

  private boolean checkPass() {
    for (int i = 0; i < 26; i++) {
      if (cur == 0 && i == 25) continue;
      if (cur == 1 && i == 0) continue;
      for (Integer d : dice) {
        int from = i;
        int to = i + (cur == 0 ? d : -d);
        if (to >= 25) to = 26;
        else if (to <= 0) to = 27;
        if (checkValid(cur, from, to)) return false;
      }
    }
    return true;
  }

  private void pass() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      System.out.println("pass sleep err");
      e.printStackTrace();
    }

    // 先换手
    cur ^= 1;

    // 然后发送信息
    addStep_pass();

    // 刷新骰子并发送信息
    flushDice();
    addStep_dice();
  }

  private void addStep_turn() {
    getMatch().broadCast(
      new JSONObject()
        .set("action", "set step truly")
        .set("data",
          new JSONObject()
            .set("step", "t"))
    );
    getSteps().append("t");
  }

  private void addStep_pass() {
    getMatch().broadCast(
      new JSONObject()
        .set("action", "set step truly")
        .set("data",
          new JSONObject()
            .set("step", "p"))
    );
    getSteps().append("p");
  }

  private void test_initG() {
    for (int i = g.length - 1; i >= 0; i--)
      g[i] = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      g[6].add(1);
      g[19].add(0);
    }
  }
}
