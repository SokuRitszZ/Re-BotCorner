package com.soku.rebotcorner.consumer;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.games.BackgammonGame;
import com.soku.rebotcorner.pojo.BackgammonRating;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.BackgammonRatingDAO;
import com.soku.rebotcorner.utils.BotDAO;
import com.soku.rebotcorner.utils.JwtAuthenticationUtil;
import com.soku.rebotcorner.utils.UserDAO;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
@ServerEndpoint("/websocket/backgammon/{token}")
public class BackgammonWebSocketServer {
  private Session session;
  private static ConcurrentHashMap<Integer, BackgammonWebSocketServer> users = new ConcurrentHashMap<>();
  private User user;
  private boolean hasClosed;
  private BackgammonGame game;

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

  private boolean findBot(int botId) {
    return BotDAO.selectOne(new QueryWrapper<Bot>().eq("id", botId).eq("game_id", 3)) != null;
  }

  private void startSingleGaming(JSONObject json) {
    Integer botId0 = json.getInteger("botId0");
    Integer botId1 = json.getInteger("botId1");
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
    game = new BackgammonGame("single", this, this, bot0, bot1);
  }
}
