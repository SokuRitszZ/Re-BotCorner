package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.RecordDAO;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 游戏抽象类
 */
@Data
public abstract class AbsGame {
  // 模式
  private String mode;
  // 匹配
  private GameMatch match;
  // 机器人
  private List<RunningBot> bots;
  // 录像文件（包括初始化数据）
  private JSONObject record;
  // 原因
  private String[] reason;
  // 结果
  private String result;
  // 游戏编号
  private Integer gameId;
  // 是否结束
  private boolean hasOver;
  // 初始数据
  private JSONObject initData;
  // 步
  private StringBuilder steps;
  // 是否已开始
  private boolean hasStart;

  // 游戏准备阶段
  /**
   * 玩家数
   *
   * @return
   */
  public abstract Integer getPlayerCount();

  /**
   * 获取所有bot的id
   *
   * @return
   */
  private List<Integer> getBotIds() {
    List<Integer> list = new ArrayList<>();
    for (RunningBot bot : this.getBots())
      if (bot != null)
        list.add(bot.getBot().getId());
      else
        list.add(0);
    return list;
  }

  /**
   * 获取初始值并且保存起来
   *
   * @return
   */
  public JSONObject getInitDataAndSave() {
    JSONObject initData = makeInitData();
    getRecord().set("initData", initData);
    return initData;
  }


  abstract protected JSONObject makeInitData();

  /**
   * 构造函数
   *
   * @param mode
   * @param match
   * @param bots
   */
  public AbsGame(String mode, GameMatch match, List<RunningBot> bots) {
    this.setMode(mode);
    this.setMatch(match);
    this.setBots(bots);

    // init reason
    this.setReason(new String[2]);

    // init record
    this.setRecord(new JSONObject());
    this.setSteps(new StringBuilder());
  }

  /**
   * 启动机器人
   */
  public void compileBots() {
    AtomicInteger atomicInteger = new AtomicInteger();
    for (RunningBot bot : this.bots) {
      if (bot != null) new Thread(() -> {
        bot.start();
        bot.compile();
        atomicInteger.incrementAndGet();
      }).start();
      else atomicInteger.getAndIncrement();
    }
    while (atomicInteger.get() != bots.size()) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * 游戏开始
   */
  public abstract void start();

  // 游戏进行时

  /**
   * 获取序列化运行数据
   *
   * @return
   */
  public abstract String parseDataString();

  /**
   * 设步前验证是否有机器人
   *
   * @param json
   */
  public void setStep(JSONObject json) {
    if (!isHasStart() || isHasOver()) return ;
    if (this.bots.get(json.getInt("id")) != null) return ;
    this._setStep(json);
  }

  /**
   * 真正的设步
   *
   * @param json
   */
  protected abstract void _setStep(JSONObject json);

  /**
   * 设置原因
   *
   * @param id
   * @param reason
   */
  public void setReason(Integer id, String reason) {
    this.reason[id] = reason;
  }

  /**
   * 结束游戏
   */
  public void gameOver() {
    if (this.hasOver) return ;
    this.hasOver = true;

    this.describeResult();

    this.tellResult();
    this.stopBots();

    this.saveRecord();
  }

  // 游戏结束

  /**
   * 保存录像
   *
   * @return
   */
  public void saveRecord() {
    try {
      Record record = new Record()
        .setGameId(gameId)
        .setUserIds(Strings.join(getUserIds(), ','))
        .setCreateTime(new Date())
        .setBotIds(Strings.join(getBotIds(), ','))
        .setRecordJson(new JSONObject()
          .set("initData", initData)
          .set("steps", steps).toString())
        .setResult(result)
        .setReason(Strings.join(Arrays.asList(reason), ','));
      RecordDAO.mapper.insert(record);
    } catch (Exception e) {
      e.printStackTrace();
      // 保存录像文件过大
    }
  }

  /**
   * 停止运行所有Bot
   */
  public void stopBots() {
    List<RunningBot> bots = this.getBots();
    for (RunningBot bot : bots)
      if (bot != null)
        bot.stop();
  }

  /**
   * 通知结果
   */
  private void tellResult() {
    this.match.broadCast(
      new JSONObject()
        .set("action", "tell result")
        .set("data",
          new JSONObject()
            .set("result", this.result)
            .set("reason", Strings.join(Arrays.asList(this.reason), ','))
        )
    );
  }

  public abstract void describeResult();

  public List<Integer> getUserIds() {
    List<Integer> list = new ArrayList<>();
    int n = getPlayerCount();
    for (int i = 0; i < n; ++i) {
      RunningBot runningBot = this.getBots().get(i);
      if (runningBot == null) {
        int j = i >= this.match.getSockets().size() ? this.match.getSockets().size() - 1 : i;
        list.add(this.match.getSockets().get(j).getUser().getId());
      } else {
        Bot bot = runningBot.getBot();
        list.add(bot.getUserId());
      }
    }
    return list;
  }
}
