package com.soku.rebotcorner.runningbot;

import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.utils.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.soku.rebotcorner.utils.RedisConstants.CACHE_BOT_KEY;

public class RunningBot {
  private final static String URL = new String[]{"http://39.107.100.77:8000/api", "http://localhost:8000/api"}[Constants.MODE];
  private final static String startBotUrl = String.format("%s/runbot/start/", URL);
  private final static String compileUrl = String.format("%s/runbot/compile/", URL);
  private final static String prepareDataUrl = String.format("%s/runbot/prepare/", URL);
  private final static String runUrl = String.format("%s/runbot/run/", URL);
  private final static String stopUrl = String.format("%s/runbot/stop/", URL);

  private static ConcurrentHashMap<String, RunningBot> bots = new ConcurrentHashMap<>();
  private Bot bot;
  private String uuid;

  public RunningBot() {
  }

  public RunningBot(Integer id) {
    this.bot = CacheClient.queryWithPassThrough(CACHE_BOT_KEY, id, Bot.class, BotDAO.mapper::selectById, 10L, TimeUnit.MINUTES);
  }

  public String getUuid() {
    return this.uuid;
  }

  public Bot getBot() {
    return bot;
  }

  public void setBot(Bot bot) {
    this.bot = bot;
  }

  public void start() {
    this.uuid = UUID.randomUUID().toString().replaceAll("-", "");
    bots.put(uuid, this);
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("uuid", uuid.toString());
    data.add("code", bot.getCode());
    data.add("lang", LangDAO.getLang(bot.getLangId()));
    RT.POST(startBotUrl, data);
  }

  public String compile() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("uuid", uuid.toString());
    return RT.POST(compileUrl, data);
  }

  public void prepareData(String data) {
    MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
    request.add("uuid", uuid.toString());
    request.add("data", data);
    RT.POST(prepareDataUrl, request);
  }

  public String run() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("uuid", uuid.toString());
    return RT.POST(runUrl, data);
  }

  public void stop() {
    new Thread(() -> {
      MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
      data.add("uuid", uuid.toString());
      RT.POST(stopUrl, data);
    }).start();
    bots.remove(this.uuid);
  }
}
