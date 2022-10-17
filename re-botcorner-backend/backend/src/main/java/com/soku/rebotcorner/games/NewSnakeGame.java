package com.soku.rebotcorner.games;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.runningbot.RunningBot;

import java.util.List;

public class NewSnakeGame extends AbsGame {
  private String mode;
  private GameMatch match;
  private List<RunningBot> bots;

  public NewSnakeGame(
    String mode,
    GameMatch match,
    List<RunningBot> bots
  ) {
    this.mode = mode;
    this.match = match;
    this.bots = bots;
  }

  @Override
  public Integer getPlayerCount() {
    return null;
  }

  @Override
  public JSONObject getInitData() {
    return null;
  }

  @Override
  public void setStep(JSONObject json) {

  }

  @Override
  public void start() {

  }

  @Override
  public String parseDataString() {
    return null;
  }

  @Override
  public JSONObject saveRecord() {
    return null;
  }
}
