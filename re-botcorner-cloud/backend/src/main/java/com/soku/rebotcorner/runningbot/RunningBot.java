package com.soku.rebotcorner.runningbot;

import com.soku.rebotcorner.consumer.SnakeWebSocketServer;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.utils.BotDAO;
import com.soku.rebotcorner.utils.LangDAO;
import com.soku.rebotcorner.utils.RT;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RunningBot {
  private final static String URL = "http://103.52.153.224:8000/api";
  private final static String startBotUrl = String.format("%s/runbot/start/", URL);
  private final static String compileUrl = String.format("%s/runbot/compile/", URL);
  private final static String prepareDataUrl = String.format("%s/runbot/prepare/", URL);
  private final static String runUrl = String.format("%s/runbot/run/", URL);
  private final static String stopUrl = String.format("%s/runbot/stop/", URL);

  private static ConcurrentHashMap<UUID, RunningBot> bots = new ConcurrentHashMap<>();
  private SnakeWebSocketServer socket;
  private Bot bot;
  private UUID uuid;

  public RunningBot() {
    this.uuid = UUID.randomUUID();
  }

  public RunningBot(Integer id, SnakeWebSocketServer socket) {
    this.uuid = UUID.randomUUID();
    this.bot = BotDAO.getById(id);
    this.socket = socket;
    bots.put(uuid, this);
  }

  public UUID getUuid() {
    return this.uuid;
  }

  public void setBot(Bot bot) {
    this.bot = bot;
  }

  public void start() {
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
  }
}
