package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.RecordDAO;
import com.soku.rebotcorner.utils.Res;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 游戏抽象类
 */
@Data
public abstract class AbsGame {
  private String mode;
  private GameMatch match;
  private List<RunningBot> bots;
  private JSONObject record;
  private String[] reason;
  private String result;
  private Integer gameId;
  private boolean hasOver;
  private StringBuilder steps;
  private boolean hasStart;

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
   * 设置原因
   *
   * @param id
   * @param reason
   */
  public void setReason(Integer id, String reason) {
    this.reason[id] = reason;
  }

  /**
   * 玩家数
   *
   * @return
   */
  abstract Integer getPlayerCount();

  /**
   * 获取初始数据
   *
   * @return
   */
  public abstract JSONObject getInitDataAndSave();

  /**
   * 设步
   *
   * @param json
   */
  public abstract void setStep(JSONObject json);

  /**
   * 游戏开始
   */
  public abstract void start();

  /**
   * 获取序列化运行数据
   *
   * @return
   */
  abstract String parseDataString();

  /**
   * 保存录像
   *
   * @return
   */
  public void saveRecord() {
    try {
      Record record = new Record(
        null,
        this.record.toString(),
        new Date(),
        this.gameId
      );
      RecordDAO.add(record);
    } catch (Exception e) {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
//      this.getMatch().broadCast(Res.fail("录像文件太大，无法保存"));
    }
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
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
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
   * 初始化录像
   */
  public void initRecord() {
    this.record.set("userIds", this.getUserIds());
    this.record.set("botIds", this.getBotIds());
    this.record.set("reason", this.reason);
  }

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
   * 通知结果
   */
  public void tellResult() {
    JSONObject ret = new JSONObject();
    JSONObject data = new JSONObject();
    StringBuilder reason = new StringBuilder();
    for (String s : this.reason) { if (s != null) reason.append(s); }
    this.match.broadCast(
      ret
        .set("action", "tell result")
        .set("data",
          data
            .set("result", this.result)
            .set("reason", reason.toString().trim())
        )
    );
  }

  /**
   * 结束游戏
   */
  public void gameOver() {
    if (this.hasOver) return ;
    this.hasOver = true;

    this.setResultToRecord();
    this.getRecord().set("steps", this.steps);
    this.initRecord();

    this.tellResult();
    this.stopBots();

    this.saveRecord();
  }

  protected abstract void setResultToRecord();

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
