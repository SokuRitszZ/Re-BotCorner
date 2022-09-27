package com.soku.rebotcorner.consumer;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.consumer.match.ReversiMatch;
import com.soku.rebotcorner.games.ReversiGame;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.pojo.ReversiRating;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.*;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
@ServerEndpoint("/websocket/reversi/{token}")  // 注意不要以'/'结尾
public class ReversiWebSocketServer {
  private final static String addUrl = "http://localhost:8081/api/matching/add/";
  private final static String removeUrl = "http://localhost:8081/api/matching/remove/";
  private static ConcurrentHashMap<Integer, ReversiWebSocketServer> users = new ConcurrentHashMap<>();
  private Session session = null;
  private ReversiGame reversiGame;
  private User user = null;
  private ReversiMatch match;
  public RunningBot bot;
  private boolean hasClosed = false;

  @OnOpen
  public void onOpen(Session session, @PathParam("token") String token) {
    this.session = session;
    Integer userId = JwtAuthenticationUtil.getUserId(token);
    users.put(userId, this);
    this.user = UserDAO.selectById(userId);
    ReversiRating reversiRating = ReversiRatingDAO.selectById(userId);
    if (reversiRating == null) {
      ReversiRatingDAO.insert(new ReversiRating(userId, 1500));
    }
    System.out.println("connected.");
  }

  @OnClose
  public void onClose() {
    hasClosed = true;
    removeMatch();
    users.remove(user.getId());
    if (match != null && reversiGame != null && !reversiGame.isGameOver()) {
      reversiGame.setReason("早退");
      reversiGame.setGameResult(1 - match.getMe(this));
      reversiGame.gameOver();
    }
    if (match != null) {
      exitMatching(new JSONObject());
    }
    System.out.println("disconnected.");
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    JSONObject json = JSONObject.parseObject(message);
    route(json);
    System.out.println("received.");
  }

  @OnError
  public void onError(Session session, Throwable error) {
    error.printStackTrace();
    removeMatch();
  }

  public static void makeMatching(Integer userId0, Integer userId1) {
    User user0 = UserDAO.selectById(userId0);
    User user1 = UserDAO.selectById(userId1);
    ReversiWebSocketServer socket0 = users.get(userId0);
    ReversiWebSocketServer socket1 = users.get(userId1);
    ReversiMatch reversiMatch = new ReversiMatch(new ReversiWebSocketServer[]{socket0, socket1});
    if (socket0 != null) {
      socket0.setMatch(reversiMatch);
      JSONObject json0 = new JSONObject();
      json0.put("action", "successMatching");
      json0.put("userId", userId1);
      json0.put("id", 1);
      json0.put("headIcon", user1.getHeadIcon());
      json0.put("username", user1.getUsername());
      socket0.sendMessage(json0);
    }
    if (socket1 != null) {
      socket1.setMatch(reversiMatch);
      JSONObject json1 = new JSONObject();
      json1.put("action", "successMatching");
      json1.put("userId", userId0);
      json1.put("id", 0);
      json1.put("headIcon", user0.getHeadIcon());
      json1.put("username", user0.getUsername());
      socket1.sendMessage(json1);
    }
  }

  public void route(JSONObject json) {
    System.out.println("route." + json.getString("action"));
    switch (json.getString("action")) {
      case "startSingleGaming": startSingleGaming(json); break;
      case "putChess": putChess(json); break;
      case "saveRecord": saveRecord(json); break;
      case "playRecord": playRecord(json); break;
      case "startMatching": startMatching(json); break;
      case "cancelMatching": cancelMatching(json); break;
      case "switchMatchOk": switchMatchOk(json); break;
      case "switchMatchNot": switchMatchNot(json); break;
      case "exitMatching": exitMatching(json); break;
      case "sendTalk": sendTalk(json); break;
    }
  }

  public void sendMessage(JSONObject json) {
    if (hasClosed) return ;
    String message = json.toJSONString();
    synchronized (this.session) {
      try {
        this.session.getBasicRemote().sendText(message);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public boolean findBot(Integer id) {
    return BotDAO.selectOne(new QueryWrapper<Bot>().eq("id", id).eq("game_id", 2)) != null;
  }

  public void startSingleGaming(JSONObject json) {
    Integer botId0 = json.getInteger("botId0");
    Integer botId1 = json.getInteger("botId1");
    RunningBot bot0 = null;
    RunningBot bot1 = null;
    json.put("result", "");
    if (botId0 > 0) {
      if (!findBot(botId0)) {
        json.put("result", json.getString("result") + "不存在黑子所选的Bot\n");
      } else {
        bot0 = new RunningBot(botId0);
      }
    }
    if (botId1 > 0) {
      if (!findBot(botId1)) {
        json.put("result", json.getString("result") + "不存在白子所选的Bot\n");
      } else {
        bot1 = new RunningBot(botId1);
      }
    }
    if (json.getString("result") != "") {
      json.put("result", json.getString("result").strip());
      sendMessage(json);
      return ;
    }
    reversiGame = new ReversiGame("single", 8, 8, this, this, bot0, bot1);
    json = reversiGame.parseData();
    json.put("action", "startSingleGaming");
    json.put("result", "ok");
    reversiGame.compileBot();
    reversiGame.start();
    sendMessage(json);
  }

  public void putChess(JSONObject json) {
    if (this.bot != null) return ;
    Integer id = json.getInteger("id");
    int r = json.getInteger("r");
    int c = json.getInteger("c");
    reversiGame.setToPut(id, new int[]{r, c});
  }

  public void saveRecord(JSONObject json) {
    if (match != null) match.broadcast(reversiGame.saveRecord());
    else sendMessage(reversiGame.saveRecord());
  }

  public void playRecord(JSONObject json) {
    if (reversiGame != null) reversiGame.setGameOver(true);
    Integer id = json.getInteger("id");
    Record record = RecordDAO.selectById(id);
    JSONObject data = JSONObject.parseObject(record.getJson());
    int rows = data.getInteger("rows");
    int cols = data.getInteger("cols");
    String reason = data.getString("reason");
    String stringifiedSteps = data.getString("steps");
    List<String> steps = List.of(stringifiedSteps.split(" "));
    reversiGame = new ReversiGame("record", rows, cols, this, this, null, null);
    reversiGame.setSteps(steps);
    reversiGame.setGameResult(record.getResult());
    reversiGame.setReason(reason);
    JSONObject backJson = reversiGame.parseData();
    backJson.put("action", "playRecord");
    sendMessage(backJson);
    reversiGame.start();
  }

  public void startMatching(JSONObject json) {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    Integer botId = json.getInteger("botId");
    if (botId != null && botId == -1) {
      bot = null;
    } else {
      bot = new RunningBot(botId);
    }
    data.add("game", "reversi");
    data.add("userId", user.getId().toString());
    data.add("rating", ReversiRatingDAO.selectById(user.getId()).getRating().toString());
    RT.POST(addUrl, data);
    json = new JSONObject();
    json.put("action", "startMatching");
    sendMessage(json);
  }

  public void cancelMatching(JSONObject json) {
    removeMatch();
    json = new JSONObject();
    json.put("action", "cancelMatching");
    sendMessage(json);
  }

  public void removeMatch() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "reversi");
    data.add("userId", user.getId().toString());
    RT.POST(removeUrl, data);
  }

  public void switchMatchOk(JSONObject json) {
    Integer id = json.getInteger("id");
    match.isOk[id] = true;
    match.broadcast(json);
    if (match.isAllOk()) {
      startMultiGaming();
    }
  }

  public void switchMatchNot(JSONObject json) {
    Integer id = json.getInteger("id");
    match.isOk[id] = false;
    match.broadcast(json);
  }

  public void exitMatching(JSONObject json) {
    if (match != null) {
      Integer id = match.getMe(this);
      json.put("action", "exitMatching");
      json.put("id", id);
      ReversiWebSocketServer uSocket = match.sockets[1 - id];
      match.broadcast(json);
      if (!match.isAllOk()) {
        uSocket.startMatching(json);
      }
      match.sockets[id ^ 1].match = null;
      match = null;
    }
  }

  public void startMultiGaming() {
    reversiGame = new ReversiGame("multi", 8, 8, match.sockets[0], match.sockets[1], match.sockets[0].bot, match.sockets[1].bot);
    match.sockets[0].reversiGame = match.sockets[1].reversiGame = reversiGame;
    JSONObject json = reversiGame.parseData();
    json.put("action", "startMultiGaming");
    reversiGame.compileBot();
    reversiGame.start();
    match.broadcast(json);
  }

  public void sendTalk(JSONObject json) {
    String content = json.getString("content");
    json = new JSONObject();
    json.put("action", "sendTalk");
    if (content.length() > 64) {
      json.put("result", "字数超过64");
      sendMessage(json);
    } else {
      json.put("result", "ok");
      json.put("userId", this.user.getId());
      json.put("username", this.user.getUsername());
      json.put("content", content);
      Date time = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      json.put("time", sdf.format(time));
      if (match != null) {
        match.broadcast(json);
      } else {
        sendMessage(json);
      }
    }
  }
}
