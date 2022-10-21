package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.Res;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import static com.soku.rebotcorner.utils.ScaleUtil.transform;

public class BackgammonGamen extends AbsGame {
  private final static Random random = new Random();

  private ReentrantLock lock;
  private Integer curId;
  private int[] inEnd;

  @Data
  static class Pair {
    int type, count;
  };

  private Pair[] chess;
  private List<Integer> dice;

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public BackgammonGamen(
    String mode,
    GameMatch match,
    List<RunningBot> bots
  ) {
    super(mode, match, bots);

    this.setGameId(3);

    // init chess
    this.dice = new ArrayList<>();
    this.createChess();
    this.flushDice();
    while (this.dice.get(0) == this.dice.get(1)) this.flushDice();
    this.curId = this.dice.get(0) < this.dice.get(1) ? 1 : 0;
    this.inEnd = new int[2];

    // init operations
    this.lock = new ReentrantLock();
  }

  /**
   * 获取骰子
   */
  private void flushDice() {
    dice.clear();
    Integer p0 = random.nextInt(6) + 1, p1 = random.nextInt(6) + 1;
    dice.add(p0);
    dice.add(p1);
    if (p0 == p1) {
      dice.add(p0);
      dice.add(p1);
    }
  }

  /**
   * 创建棋盘
   */
  private void createChess() {
    chess = new Pair[26];
    for (int i = 0; i < 26; ++i) {
      chess[i] = new Pair();
      chess[i].type = 2;
      chess[i].count = 0;
    }
    // for test
//    chess[0].type = 0;
//    chess[25].type = 1;
//    chess[1].type = 1;
//    chess[1].count = 15;
//    chess[24].type = 0;
//    chess[24].count = 15;

    // for test
//    chess[0].type = 0;
//    chess[25].type = 1;
//    for (int i = 1; i <= 6; ++i) {
//      chess[i].type = 1;
//      chess[i].count = 3;
//    }
//    chess[0].count = 15;

    // 老家是 0(白) / 25(红), 终点是 25(白), 0(红)
    chess[0].type = chess[1].type = chess[12].type = chess[17].type = chess[19].type = 0;
    chess[6].type = chess[8].type = chess[13].type = chess[24].type = chess[25].type = 1;
    chess[1].count = chess[24].count = 2;
    chess[8].count = chess[17].count = 3;
    chess[6].count = chess[12].count = chess[13].count = chess[19].count = 5;
  }

  /**
   * 获取玩家数
   *
   * @return
   */
  @Override
  Integer getPlayerCount() {
    return 2;
  }

  /**
   * 获取初始信息并保存
   *
   * @return
   */
  @Override
  public JSONObject getInitDataAndSave() {
    JSONObject json = new JSONObject();
    json.set("initChess", this.chess);
    json.set("initStart", this.curId);
    json.set("initDice", this.dice);
    this.getRecord().set("initData", json);
    return json;
  }

  /**
   * 设步
   *
   * @param json
   */
  @Override
  public void setStep(JSONObject json) {
    if (this.isHasOver() || !this.isHasStart()) return ;
    Integer id = json.getInt("id");
    if (id != 2 && this.getBots().get(id) != null) return ;
    Integer from = json.getInt("from");
    Integer to = json.getInt("to");
    this.checkAndMoveChess(id, from, to);
  }

  /**
   * 检测移动是否合法
   *
   * @param id
   * @param from
   * @param to
   * @return
   */
  private boolean checkValid(int id, int from, int to) {
    // 移动的棋子不是合法的
    if (id != curId || id == 2) return false;
    int step = to - from;
    if (id == 1) step *= -1;
    // 老家
    int home = id == 0 ? 0 : 25;
    // 终点
    int end = id == 0 ? 25 : 0;
    // 如果去的不是终点，就要严格跟着有的点数来走
    if (to != end && !this.canUse(step)) {
//      System.out.println(1 + " " + this.dice + " " + to);
      return false;
    }
    // 如果移动的地方不是自己的棋子，或者没有棋子
    if (chess[from].type != id || chess[from].count == 0) {
//      System.out.println(2 + " " + chess[from].type + chess[from].count);
      return false;
    }
    // 如果家里有棋子，但是不是从家里出来的
    if (chess[home].count > 0 && from != home) {
//      System.out.println(3 + " " + home + " " + chess[home].count);
      return false;
    }
    if (to == end) {
      // 走至终点，要保证本区之外没有自己的棋子
      int l = id == 0 ? 1 : 7;
      int r = id == 0 ? 18 : 24;
      for (int i = l; i <= r; ++i)
        if (chess[i].type == id)
          return false;
//      System.out.println(4);
      // 都在本区内，如果最大的点数无法让最外的子恰好走到终点就可以用
      // 如果有可以恰好走终点的就可以直接同意
      if (!this.canUse(step)) {
        boolean flag = false;
        // 要保证动的是最外的
        int ll = id == 0 ? 19 : 6;
        int type = id == 0 ? 1 : -1;
        for (int i = ll; i != from; i += type)
          if (chess[i].type == id)
            return false;
        // 如果找到这个最大的点数就可以
        for (Integer d : dice) if (d > step) {
          flag = true;
          break;
        }
        // 都不行，那就不行
        if (!flag) return false;
//        System.out.println(5);
      }
    }
    // 如果走的是敌人的阵营，不能
    if (to != end && chess[to].type == (id ^ 1) && chess[to].count > 1) {
//      System.out.println(6 + " " + chess[to].count);
      return false;
    }
    return true;
  }

  /**
   * 检查使用骰子
   *
   * @param step
   * @return
   */
  private boolean canUse(int step) {
    for (Integer d: dice) if (d == step) return true;
    return false;
  }

  /**
   * 检查并移动
   *
   * @param id
   * @param from
   * @param to
   */
  private void checkAndMoveChess(Integer id, Integer from, Integer to) {
    if (!checkValid(id, from, to)) {
      this.getMatch().sendOne(id == 2 ? 0 : id, Res.fail("操作不合法，请重试"));
      return ;
    }
    this.moveChess(id, from, to);
  }

  /**
   * 移动棋子
   *
   * @param id
   * @param from
   * @param to
   */
  public void moveChess(int id, int from, int to) {
    if (this.isHasOver()) return ;
    --chess[from].count;
    int home = id == 0 ? 0 : 25;
    int end = 25 - home;
    // 不是从老家来的，并且把它清空了
    if (from != home && chess[from].count == 0) chess[from].type = 2;
    // 走到终点了
    if (to == end) ++inEnd[id];
    // 走到自己的阵营
    else if (chess[to].type == id) ++chess[to].count;
    // 走到敌人的阵营
    else if (chess[to].type == (id ^ 1)) {
      chess[to].type = id;
      // 把敌人吃回家
      ++chess[end].count;
    }
    // 走到空地
    else if (chess[to].type == 2) {
      chess[to].type = id;
      ++chess[to].count;
    }
    int step = to - from;
    if (id == 1) step *= -1;
    boolean flag = false;
    for (int i = 0; i < dice.size(); ++i)
      if (dice.get(i) == step) {
        dice.remove(i);
        flag = true;
        break;
      }
    if (!flag) {
      for (int i = 0; i < dice.size(); ++i) {
        if (dice.get(i) > step) {
          dice.remove(i);
          break;
        }
      }
    }
    JSONObject ret = new JSONObject();
    ret.set("action", "moveChess");
    ret.set("from", from);
    ret.set("to", to);

    // 保存到录像
    getSteps().append("mv" + transform(from, 36) + transform(to, 36) + "\n");

    this.getMatch().broadCast(Res.ok(ret));

    // 添加骰子的变化
    this.getMatch().broadCast(Res.ok(getCurrentDiceToSet()));

    // 保存到录像
    getSteps().append("dc" + curId);
    for (Integer die : this.dice) getSteps().append("" + die);
    getSteps().append("\n");

    new Thread(() -> {
      this.nextStep();
    }).start();
  }

  /**
   * 下一步
   */
  private void nextStep() {
    // 检测是否已经结束
    if (this.checkOverAndSaveResult()) {
      this.gameOver();
      return ;
    }
    // 检测是否轮到对方
    if (this.checkTurn()) this.turn();
    // 跳过，直到可以
    while (this.checkPass()) this.pass();
    // 检测是否需要运行Bot
    this.runBot();
  }

  /**
   * 设置骰子
   *
   * @return
   */
  private JSONObject getCurrentDiceToSet() {
    JSONObject json = new JSONObject();
    json.set("action", "setDice");
    json.set("dice", dice);
    json.set("curId", curId);
    return json;
  }

  /**
   * 开始游戏
   */
  @Override
  public void start() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    this.setHasStart(true);
    this.flushDice();
    JSONObject json = this.getCurrentDiceToSet();
    this.getMatch().broadCast(Res.ok(json));

    // 保存到录像
    getSteps().append("dc" + curId);
    for (Integer die : this.dice) getSteps().append("" + die);
    getSteps().append("\n");
    this.nextStep();
  }

  /**
   * 运行Bot
   */
  private void runBot() {
    if (this.isHasOver()) return ;
    RunningBot bot = this.getBots().get(this.curId);
    if (bot != null) {
      bot.prepareData(parseDataString());
      JSONObject json = JSONUtil.parseObj(bot.run());
      String input = json.getStr("data");
      String runResult = this.checkInput(input);
      if (runResult != "ok") {
        this.setReason(curId, runResult);
        this.gameOver();
      } else {
        String[] rc = input.split(" ");
        int from = Integer.parseInt(rc[0]);
        int to = Integer.parseInt(rc[1]);
        this.moveChess(curId, from, to);
      }
    }
  }

  /**
   * 检测输出是否合法
   *
   * @param input
   * @return
   */
  public String checkInput(String input) {
    if (input == null) return "运行超时";
    input = input.trim();
    if (input.length() == 0) return "输出为空";
    Pattern pattern = Pattern.compile("^((\\d)|([1-9]\\d*))\\s((\\d)|([1-9]\\d*))$");
    if (!pattern.matcher(input).matches())
      return String.format("非法输出0: %s", input);
    String[] rc = input.split(" ");
    int from = Integer.parseInt(rc[0]);
    int to = Integer.parseInt(rc[1]);
    if (from < 0 || from >= 26 || to < 0 || to >= 26)
      return String.format("非法输出1: %s", input);
    if (!checkValid(curId, from, to))
      return String.format("非法操作: %s", input);
    return "ok";
  }

  /**
   * 跳过
   */
  private void pass() {
    flushDice();
    curId ^= 1;
    JSONObject json = new JSONObject();
    json.set("action", "pass");
    json.set("passed", curId ^ 1);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    this.getMatch().broadCast(Res.ok(json));
    // 保存到录像
    getSteps().append("ps" + curId + "\n");

    this.getMatch().broadCast(Res.ok(this.getCurrentDiceToSet()));

    // 保存到录像
    getSteps().append("dc" + curId);
    for (Integer die : this.dice) getSteps().append("" + die);
    getSteps().append("\n");
  }

  /**
   * 检测是否跳过
   *
   * @return
   */
  private boolean checkPass() {
    for (int i = 0; i < 26; ++i) {
      if (curId == 0 && i == 25) continue;
      if (curId == 1 && i == 0) continue;
      for (Integer d: dice) {
        int from = i;
        int to = i + (curId == 0 ? d : -d);
        if (to < 0) to = 0;
        if (to > 25) to = 25;
        if (this.checkValid(curId, from, to)) return false;
      }
    }
    return true;
  }

  /**
   * 换人
   */
  private void turn() {
    this.flushDice();
    curId ^= 1;

    // 保存到录像
    getSteps().append("tn" + curId + "\n");

    this.getMatch().broadCast(Res.ok(this.getCurrentDiceToSet()));
    // 保存到录像
    getSteps().append("dc" + curId);
    for (Integer die : this.dice) getSteps().append("" + die);
    getSteps().append("\n");
  }

  /**
   * 检测是否换人
   *
   * @return
   */
  private boolean checkTurn() {
    return dice.size() == 0;
  }

  /**
   * 检测游戏是否结束
   *
   * @return
   */
  private boolean checkOverAndSaveResult() {
    boolean isOver = this.inEnd[0] == 15 || this.inEnd[1] == 15;
    if (!isOver) return false;
    // 游戏确实结束了

    int winner = this.inEnd[0] == 15 ? 0 : 1;
    int loser = winner ^ 1;

    // 胜利的程度
    int loserHome = loser == 0 ? 0 : 25;
    String result = "";
    if (inEnd[loser] == 0) {
      if (chess[loserHome].count > 0 || checkInner(winner)) result = "完胜";
      else result = "全胜";
    } else result = "单胜";
    result = (winner == 0 ? "白" : "红") + result;
    this.setResult(result);
    this.setReason(winner, "获胜");
    this.setReason(loser, "战败");
    return true;
  }

  /**
   * 检测本区
   *
   * @param id
   * @return
   */
  private boolean checkInner(int id) {
    if (id == 0) {
      for (int i = 19; i <= 24; ++i)
        if (chess[i].type == 1)
          return true;
    } else {
      for (int i = 1; i <= 6; ++i)
        if (chess[i].type == 0)
          return true;
    }
    return false;
  }

  /**
   * 获取序列化数据
   *
   * @return
   */
  @Override
  String parseDataString() {
    String data = curId + " ";
    for (int i = 0; i < 26; ++i)
      data += chess[i].type + " " + chess[i].count + " ";
    data += dice.size() + " ";
    for (Integer d: dice)
      data += d + " ";
    return data.trim();
  }

  /**
   * 将结果写入record
   */
  @Override
  public void setResultToRecord() {
    // 有可能是因为中途退出了
    if (this.getResult() == null || this.getResult().length() == 0) {
      if (this.getReason()[0] == null || this.getReason()[0].length() == 0)
        this.setResult("白获胜");
      else
        this.setResult("红获胜");
    }
    this.getRecord().set("result", this.getResult());
  }
}
