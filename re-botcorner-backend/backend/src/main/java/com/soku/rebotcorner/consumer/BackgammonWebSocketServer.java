package com.soku.rebotcorner.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.consumer.match.BackgammonMatch;
import com.soku.rebotcorner.games.BackgammonGame;
import com.soku.rebotcorner.pojo.BackgammonRating;
import com.soku.rebotcorner.pojo.Bot;
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
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
@ServerEndpoint("/websocket/backgammon/{token}")
public class BackgammonWebSocketServer {
  private final static String addUrl = "http://localhost:8081/api/matching/add/";
  private final static String removeUrl = "http://localhost:8081/api/matching/remove/";
  private Session session;
  private static ConcurrentHashMap<Integer, BackgammonWebSocketServer> users = new ConcurrentHashMap<>();
  private User user;
  public boolean hasClosed;
  private BackgammonGame game;
  private BackgammonMatch match;
  private RunningBot bot;

  public static void makeMatching(Integer userId0, Integer userId1) {
    User user0 = UserDAO.selectById(userId0);
    User user1 = UserDAO.selectById(userId1);
    BackgammonWebSocketServer socket0 = users.get(userId0);
    BackgammonWebSocketServer socket1 = users.get(userId1);
    BackgammonMatch backgammonMatch = new BackgammonMatch(socket0, socket1);
    if (socket0 != null) {
      socket0.setMatch(backgammonMatch);
      JSONObject json0 = new JSONObject();
      json0.put("action", "successMatching");
      json0.put("id", userId1);
      json0.put("playId", 0);
      json0.put("headIcon", user1.getHeadIcon());
      json0.put("username", user1.getUsername());
      socket0.sendMessage(json0);
    }
    if (socket1 != null) {
      socket1.setMatch(backgammonMatch);
      JSONObject json1 = new JSONObject();
      json1.put("action", "successMatching");
      json1.put("id", userId0);
      json1.put("playId", 1);
      json1.put("headIcon", user0.getHeadIcon());
      json1.put("username", user0.getUsername());
      socket1.sendMessage(json1);
    }
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("token") String token) {
    this.session = session;
    Integer userId = JwtAuthenticationUtil.getUserId(token);
    users.put(userId, this);
    this.user = UserDAO.selectById(userId);
    BackgammonRating backgammonRating = BackgammonRatingDAO.selectById(userId);
    if (backgammonRating == null) {
      BackgammonRatingDAO.insert(new BackgammonRating(userId, 1500));
    }
    System.out.println("connected.");
  }

  @OnClose
  public void onClose() {
    hasClosed = true;
    users.remove(user.getId());
    removeMatch();
    if (this.game != null) {
      this.game.gameOver("中退", 1 - match.getMe(this));
      this.game = null;
    }
    System.out.println("received");
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    JSONObject json = JSONObject.parseObject(message);
    System.out.println("received.");
    route(json);
  }

  @OnError
  public void onError(Session session, Throwable error) {
    error.printStackTrace();
  }

  private void route(JSONObject json) {
    String action = json.getString("action");
    System.out.println("route to " + action);
    switch (action) {
      case "startSingleGaming": startSingleGaming(json); break;
      case "moveChess": moveChess(json); break;
      case "saveRecord": saveRecord(json); break;
      case "startMatching": startMatching(json); break;
      case "cancelMatching": cancelMatching(json); break;
      case "matchOk": matchOk(json); break;
      case "matchNot": matchNot(json); break;
      case "exitMatching": exitMatching(json); break;
      case "sendTalk": sendTalk(json); break;
    }
  }

  private void sendTalk(JSONObject json) {
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

  private void exitMatching(JSONObject json) {
    if (match != null) {
      Integer id = match.getMe(this);
      json.put("action", "exitMatching");
      json.put("playId", id);
      BackgammonWebSocketServer uSocket = match.sockets[1 - id];
      match.broadcast(json);
      if (!match.isAllOk()) uSocket.startMatching(json);
      uSocket.match = null;
      match = null;
    }
  }

  private void matchNot(JSONObject json) {
    int id = match.getMe(this);
    match.isOk[id] = false;
    json.put("playId", id);
    match.broadcast(json);
  }

  private void matchOk(JSONObject json) {
    int id = match.getMe(this);
    match.isOk[id] = true;
    json.put("playId", id);
    match.broadcast(json);
    if (match.isAllOk()) startMultiGaming();
  }

  private void cancelMatching(JSONObject json) {
    removeMatch();
    json = new JSONObject();
    json.put("action", "cancelMatching");
    sendMessage(json);
  }

  private void removeMatch() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "backgammon");
    data.add("userId", user.getId().toString());
    RT.POST(removeUrl, data);
  }

  private void startMatching(JSONObject json) {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    Integer botId = json.getInteger("botId");
    if (botId != null && botId == 0) bot = null;
    else bot = new RunningBot(botId);
    data.add("game", "backgammon");
    data.add("userId", user.getId().toString());
    data.add("rating", BackgammonRatingDAO.selectById(user.getId()).getRating().toString());
    RT.POST(addUrl, data);
    json = new JSONObject();
    json.put("action", "startMatching");
    sendMessage(json);
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

  private boolean findBot(int botId) {
    return BotDAO.selectOne(new QueryWrapper<Bot>().eq("id", botId).eq("game_id", 3)) != null;
  }

  private void startSingleGaming(JSONObject json) {
    Integer botId0 = json.getInteger("botId0");
    Integer botId1 = json.getInteger("botId1");
    match = new BackgammonMatch(this, this);
    json = new JSONObject();
    json.put("action", "startSingleGaming");
    StringBuilder result = new StringBuilder("");
    RunningBot bot0 = null;
    RunningBot bot1 = null;
    if (botId0 > 0)
      if (!findBot(botId0)) result.append("不存在白方所选的Bot\n");
      else bot0 = new RunningBot(botId0);
    if (botId1 > 0)
      if (!findBot(botId1)) result.append("不存在红方所选的Bot\n");
      else bot1 = new RunningBot(botId1);
    if (result.toString().length() != 0) {
      json.put("result", result);
      sendMessage(json);
      return ;
    }
    game = new BackgammonGame("single", match, bot0, bot1);
    String stringifiedData = game.parseDataString();
    json.put("stringifiedData", stringifiedData);
    json.put("result", "ok");
    try {
      game.compileBot();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    game.start();
    match.broadcast(json);
  }

  private void startMultiGaming() {
    match.sockets[0].game = match.sockets[1].game = new BackgammonGame("multi", match, match.sockets[0].bot, match.sockets[1].bot);
    String stringifiedData = game.parseDataString();
    JSONObject json = new JSONObject();
    json.put("action", "startMultiGaming");
    json.put("stringifiedData", stringifiedData);
    try {
      game.compileBot();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    game.start();
    match.broadcast(json);
  }

  private void moveChess(JSONObject json) {
    JSONObject ret = new JSONObject();
    ret.put("action", "moveChess");
    int id = json.getInteger("id");
    if (id == 0 && game.bot0 != null) return ;
    if (id == 1 && game.bot1 != null) return ;
    int from = json.getInteger("from");
    int to = json.getInteger("to");
    if (!game.checkMoveChess(id, from, to)) {
      ret.put("result", "fail");
      sendMessage(ret);
    } else {
      game.moveChess(id, from, to);
    }
  }

  private void saveRecord(JSONObject json) {
    sendMessage(game.saveRecord());
  }
}
