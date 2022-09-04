package com.soku.rebotcorner.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.games.SnakeGame;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.Game;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.utils.JwtAuthenticationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/snake/{token}")  // 注意不要以'/'结尾
public class SnakeWebSocketServer {
  private static ConcurrentHashMap<Integer, SnakeWebSocketServer> users = new ConcurrentHashMap<>();
  private static UserMapper userMapper;

  private Session session = null;
  private User user = null;

  @Autowired
  public void setUserMapper(UserMapper userMapper) {
    SnakeWebSocketServer.userMapper = userMapper;
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("token") String token) {
    this.session = session;
    System.out.println("connected.");
    Integer userId = JwtAuthenticationUtil.getUserId(token);
    this.user = userMapper.selectById(userId);
    users.put(userId, this);
  }

  @OnClose
  public void onClose() {
    System.out.println("disconnected.");
    if (this.user != null) {
      users.remove(this.user.getId());
    }
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    System.out.println("received message.");
    JSONObject json = JSONObject.parseObject(message);
    String action = json.getString("action");
    if ("startSingleGaming".equals(action)) {
      startSingleGaming();
    }
  }

  @OnError
  public void onError(Session session, Throwable error) {
    error.printStackTrace();
  }

  public void sendMessage(String message) {
    synchronized (this.session) {
      try {
        this.session.getBasicRemote().sendText(message);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void startSingleGaming() {
    SnakeGame snakeGame = new SnakeGame(12, 13, 20);
    snakeGame.createMap();
    JSONObject json = new JSONObject();
    json.put("action", "startSingleGaming");
    json.put("map", snakeGame.getG());
    sendMessage(json.toJSONString());
  }
}