package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 游戏抽象类
 */
public abstract class AbsGame {

  private GameMatch match;
  private List<RunningBot> bots;

  public void setMatch(GameMatch match){
    this.match = match;
  };

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
  public abstract JSONObject getInitData();

  /**
   * 设步
   *
   * @param json
   */
  abstract void setStep(JSONObject json);

  /**
   * 游戏开始
   */
  abstract void start();

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
  abstract JSONObject saveRecord();

  /**
   * 启动机器人
   */
  public void compileBots() {
    AtomicInteger atomicInteger = new AtomicInteger();
    for (RunningBot bot : bots) {
      if (bot != null)
        new Thread(() -> {
          bot.start();
          bot.compile();
          atomicInteger.incrementAndGet();
        }).start();
      else
        atomicInteger.getAndIncrement();
    }
    while (atomicInteger.get() != bots.size()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
