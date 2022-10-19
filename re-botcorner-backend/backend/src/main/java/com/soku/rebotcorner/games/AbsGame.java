package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.consumer.match.GameMatch;
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
  private GameMatch match;
  private List<RunningBot> bots;
  private JSONObject record;
  private String[] reason;
  private String result;

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
    Record record = new Record(
      null,
      this.record.toString(),
      new Date(),
      1
    );
    RecordDAO.add(record);
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
    this.record.set("userIds", this.match.getUserIds());
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
    JSONObject json = new JSONObject();
    json.set("action", "tellResult");
    StringBuilder message = new StringBuilder("");
    for (String s : this.reason)
      if (s != null) message.append(s).append("\n");
      else message.append("获胜").append("\n");
    json.set("reason", message.toString().trim());
    json.set("result", this.result);
    this.match.broadCast(Res.ok(json));
  }

  /**
   * 结束游戏
   */
  public abstract void gameOver();
}
