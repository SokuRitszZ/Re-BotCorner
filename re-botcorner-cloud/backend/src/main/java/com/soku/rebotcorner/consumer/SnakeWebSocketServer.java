package com.soku.rebotcorner.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.games.SnakeGame;
import com.soku.rebotcorner.mapper.RecordMapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.utils.JwtAuthenticationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/snake/{token}")  // 注意不要以'/'结尾
public class SnakeWebSocketServer {
  private final static String addUrl = "http://localhost:8081/matching/add/";
  private final static String removeUrl = "http://localhost:8081/matching/remove/";
  private static ConcurrentHashMap<Integer, SnakeWebSocketServer> users = new ConcurrentHashMap<>();
  private static UserMapper userMapper;
  private static RecordMapper recordMapper;
  private static RestTemplate restTemplate;
  private Session session = null;
  private User user = null;
  private SnakeGame snakeGame;
  private SnakeMatch snakeMatch;

  @Autowired
  public void setUserMapper(UserMapper userMapper) {
    SnakeWebSocketServer.userMapper = userMapper;
  }

  @Autowired
  public void setRecordMapper(RecordMapper recordMapper) {
    SnakeWebSocketServer.recordMapper = recordMapper;
  }

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    SnakeWebSocketServer.restTemplate = restTemplate;
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
    if (snakeGame != null) {
      if (snakeGame.getResult() == null) {
        snakeGame.setResult(1 - getMe());
        snakeGame.setOver(true);
      }
    }
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    JSONObject json = JSONObject.parseObject(message);
    String action = json.getString("action");
    System.out.println("received message." + action);
    if ("startSingleGaming".equals(action)) {
      startSingleGaming();
    } else if ("moveSnake".equals(action)) {
      if (json.getInteger("id") == 0) {
        snakeGame.setDirection0(json.getInteger("direction"));
        setDirection(0);
      } else {
        snakeGame.setDirection1(json.getInteger("direction"));
        setDirection(1);
      }
    } else if ("startMatching".equals(action)) {
      startMatching();
    } else if ("cancelMatching".equals(action)) {
      cancelMatching();
    } else if ("matchOk".equals(action)) {
      matchOk();
    } else if ("matchNotOk".equals(action)) {
      matchNotOk();
    } else if ("exitMatching".equals(action)) {
      exitMatching();
    } else if ("playRecord".equals(action)) {
      playRecord(json.getInteger("id"));
    } else if ("saveRecord".equals(action)) {
      snakeGame.saveRecord();
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
    snakeGame = new SnakeGame("single", 12, 13, 20, this, this);
    snakeMatch = new SnakeMatch(this, this);
    snakeMatch.sockets[0].snakeGame = snakeGame;
    snakeMatch.sockets[1].snakeGame = snakeGame;
    JSONObject json = new JSONObject();
    json.put("action", "startSingleGaming");
    json.put("map", snakeGame.getG());
    json.put("userId0", user.getId());
    json.put("userId1", user.getId());
    sendMessage(json.toJSONString());
    snakeGame.start();
  }

  public void startMatching() {
    /**
     * 加入匹配池
     */
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "snake");
    data.add("userId", user.getId().toString());
    data.add("rating", user.getRating().toString());
    restTemplate.postForObject(addUrl, data, String.class);
    JSONObject json = new JSONObject();
    json.put("action", "startMatching");
    sendMessage(json.toJSONString());
  }

  public static void makeMatching(Integer userId0, Integer userId1) {
    User user0 = userMapper.selectById(userId0);
    User user1 = userMapper.selectById(userId1);
    SnakeWebSocketServer socket0 = users.get(userId0);
    SnakeWebSocketServer socket1 = users.get(userId1);
    SnakeMatch snakeMatch = new SnakeMatch(socket0, socket1);
    socket0.snakeMatch = snakeMatch;
    socket1.snakeMatch = snakeMatch;

    /** 信息互发 */
    JSONObject json0 = new JSONObject();
    json0.put("action", "successMatching");
    json0.put("opponentUserId", userId1);
    json0.put("opponentId", 1);
    json0.put("opponentHeadIcon", user1.getHeadIcon());
    json0.put("opponentUsername", user1.getUsername());
    socket0.sendMessage(json0.toJSONString());

    JSONObject json1 = new JSONObject();
    json1.put("action", "successMatching");
    json1.put("opponentUserId", userId0);
    json1.put("opponentId", 0);
    json1.put("opponentHeadIcon", user0.getHeadIcon());
    json1.put("opponentUsername", user0.getUsername());
    socket1.sendMessage(json1.toJSONString());
  }

  /**
   * 取消匹配
   */
  private void cancelMatching() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "snake");
    data.add("userId", user.getId().toString());
    restTemplate.postForObject(removeUrl, data, String.class);
    JSONObject json = new JSONObject();
    json.put("action", "cancelMatching");
    sendMessage(json.toJSONString());
  }

  private void matchOk() {
    int me = getMe();
    int you = 1 - me;
    SnakeWebSocketServer youSocket = snakeMatch.sockets[you];
    JSONObject json = new JSONObject();
    json.put("action", "matchOk");
    json.put("id", me);
    sendMessage(json.toJSONString());
    youSocket.sendMessage(json.toJSONString());
    snakeMatch.isOk[me] = true;
    if (snakeMatch.allOk()) {
      startMultiGaming();
    }
  }

  private void matchNotOk() {
    int me = getMe();
    int you = 1 - me;
    SnakeWebSocketServer youSocket = snakeMatch.sockets[you];
    JSONObject json = new JSONObject();
    json.put("action", "matchNotOk");
    json.put("id", me);
    sendMessage(json.toJSONString());
    youSocket.sendMessage(json.toJSONString());
    snakeMatch.isOk[me] = false;
  }

  /**
   * 退出房间，并且将另一位玩家重新放入匹配池
   */
  private void exitMatching() {
    int me = getMe();
    int you = 1 - me;
    SnakeWebSocketServer youSocket = snakeMatch.sockets[you];
    JSONObject json = new JSONObject();

    /** 重新放入匹配池 */
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("userId", youSocket.user.getId().toString());
    data.add("rating", youSocket.user.getRating().toString());
    restTemplate.postForObject(addUrl, data, String.class);

    snakeMatch = null;
    youSocket.snakeMatch = null;
    json.put("action", "exitMatching");
    json.put("id", me);
    sendMessage(json.toJSONString());
    youSocket.sendMessage(json.toJSONString());
  }

  public void startMultiGaming() {
    snakeGame = new SnakeGame("multi", 12, 13, 20, snakeMatch.sockets[0], snakeMatch.sockets[1]);
    snakeMatch.sockets[0].snakeGame = snakeGame;
    snakeMatch.sockets[1].snakeGame = snakeGame;
    JSONObject json = new JSONObject();
    json.put("action", "startMultiGaming");
    json.put("map", snakeGame.getG());
    json.put("userId0", snakeMatch.sockets[0].user.getId());
    json.put("userId1", snakeMatch.sockets[1].user.getId());
    snakeMatch.sockets[0].sendMessage(json.toJSONString());
    if ("multi".equals(snakeGame.getMode()))
      snakeMatch.sockets[1].sendMessage(json.toJSONString());
    snakeGame.start();
  }

  public void setDirection(int id) {
    JSONObject json = new JSONObject();
    json.put("action", "setDirection");
    json.put("id", id);
    snakeMatch.sockets[0].sendMessage(json.toJSONString());
    if ("multi".equals(snakeGame.getMode()))
      snakeMatch.sockets[1].sendMessage(json.toJSONString());
  }

  public void playRecord(Integer id) {
    Record record = recordMapper.selectById(id);
    JSONObject json = JSON.parseObject(record.getJson());
    JSONArray array0 = json.getJSONArray("map");
    int[][] map = new int[array0.size()][];
    for (int i = 0; i < array0.size(); ++i) {
      JSONArray array1 = array0.getJSONArray(i);
      map[i] = new int[array1.size()];
      for (int j = 0; j < array1.size(); ++j) {
        map[i][j] = array1.getInteger(j);
      }
    }
    List<int[]> steps = new ArrayList<>();
    JSONArray array2 = json.getJSONArray("steps");
    for (int i = 0; i < array2.size(); ++i) {
      JSONArray array3 = array2.getJSONArray(i);
      int[] step = new int[2];
      for (int j = 0; j < 2; ++j) {
        step[j] = array3.getInteger(j);
      }
      steps.add(step);
    }
    if (snakeGame != null) {
      snakeGame.setOver(true);
    }
    snakeGame = new SnakeGame("record", map.length, map[0].length, 0, this, this);
    snakeGame.setG(map);
    snakeGame.setResult(record.getResult());
    snakeMatch = new SnakeMatch(this, this);
    snakeMatch.sockets[0].snakeGame = snakeGame;
    snakeMatch.sockets[1].snakeGame = snakeGame;
    JSONObject backJson = new JSONObject();
    backJson.put("action", "playRecord");
    backJson.put("map", map);
    sendMessage(backJson.toJSONString());
    snakeGame.setSteps(steps);
    snakeGame.start();
  }

  private int getMe() {
    return snakeMatch.sockets[1] == this ? 1 : 0;
  }

  public User getUser() {
    return user;
  }
}

class SnakeMatch {
  public SnakeWebSocketServer[] sockets;
  public boolean[] isOk;

  SnakeMatch(SnakeWebSocketServer socket0, SnakeWebSocketServer socket1) {
    sockets = new SnakeWebSocketServer[2];
    sockets[0] = socket0;
    sockets[1] = socket1;
    isOk = new boolean[2];
    isOk[0] = isOk[1] = false;
  }

  public boolean allOk() {
    return isOk[0] && isOk[1];
  }
};