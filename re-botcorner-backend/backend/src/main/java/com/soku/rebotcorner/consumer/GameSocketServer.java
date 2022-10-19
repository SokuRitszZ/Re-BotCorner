package com.soku.rebotcorner.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.games.AbsGame;
import com.soku.rebotcorner.games.NewSnakeGame;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.SnakeRating;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Component
@ServerEndpoint("/websocket/test/{game}/{token}")  // 注意不要以'/'结尾
public class GameSocketServer {
  private final static String addUrl = "http://localhost:8081/api/matching/add/";
  private final static String removeUrl = "http://localhost:8081/api/matching/remove/";

  private static ExecutorService es = Executors.newFixedThreadPool(10);
  private static ConcurrentHashMap<Integer, GameSocketServer> users = new ConcurrentHashMap<>();

  private Session session;
  private User user;
  private boolean hasClosed;
  private GameMatch match;
  private String gameClass;
  private RunningBot bot;
  private ReentrantLock lock = new ReentrantLock();

  private Queue<Thread> threadQueue = new LinkedList<>();

  public static void makeMatching(List<Integer> userIds) {
    // 获取基本信息
    List<JSONObject> jsons = new ArrayList<>();
    // 获取socket
    List<GameSocketServer> sockets = new ArrayList<>();
    for (Integer userId : userIds) {
      User user = UserDAO.selectById(userId);
      JSONObject json = new JSONObject();
      json.set("id", user.getId());
      json.set("username", user.getUsername());
      json.set("headIcon", user.getHeadIcon());
      jsons.add(json);

      GameSocketServer socket = users.get(userId);
      sockets.add(socket);
    }
    // 生成Matching
    GameMatch match = new GameMatch(sockets);
    // 广播
    JSONObject json = new JSONObject();
    json.set("infos", jsons);
    json.set("action", "successMatching");
    match.broadCast(Res.ok(json));
  }

  public void setMatch(GameMatch match) {
    this.match = match;
  }

  AbsGame createGameObject(String mode, GameMatch match, List<RunningBot> bots) {
    switch (this.gameClass) {
      case "snake": return new NewSnakeGame(mode, match, bots);
    }
    return null;
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("game") String game, @PathParam("token") String token) {
    this.session = session;
    Integer userId = JwtAuthenticationUtil.getUserId(token);

    this.initMatch();

    System.out.println(String.format("%d connected.", userId));
    this.user = UserDAO.selectById(userId);
    users.put(userId, this);
    this.gameClass = game;

    SnakeRating snakeRating = SnakeRatingDAO.selectById(userId);
    if (snakeRating == null) SnakeRatingDAO.insert(new SnakeRating(userId, 1500));
  }

  @OnClose
  public void onClose() {
    this.hasClosed = true;
    if (this.match != null) {
      AbsGame game = this.match.getGame();
      if (game != null) {
        game.setReason(this.match.getMe(this), "中退");
        game.gameOver();
      }
    }
    if (this.user != null) {
      users.remove(this.user.getId());
      removeFromMatch();
      System.out.println(String.format("%d disconnected.", this.user.getId()));
    }
  }

  @OnError
  public void onError(Session session, Throwable error) {
    error.printStackTrace();
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    JSONObject json = JSONUtil.parseObj(message);
    this.route(json);
  }

  public void sendMessage(JSONObject json) {
    if (hasClosed) return ;
    lock.lock();
    synchronized (this.session) {
      try {
        this.session.getBasicRemote().sendText(json.toString());
      } catch (Exception e) {
        System.out.println("e.getMessage() = " + e.getMessage());
      }
    }
    lock.unlock();
  }

  public void initMatch() {
    ArrayList<GameSocketServer> sockets = new ArrayList<>();
    sockets.add(this);
    this.match = new GameMatch(sockets);
  }

  void route(JSONObject json) {
    String action = json.getStr("action");
    System.out.println(this.user.getId() + " action = " + action);
    Res res = null;
    switch (action) {
      case "startSingleGaming": res = startSingleGaming(json); break;
      case "sendTalk": res = sendTalk(json); break;
      case "startMatching": res = startMatching(json); break;
      case "cancelMatching": res = cancelMatching(json); break;
      case "matchOk": res = matchOk(json); break;
      case "matchNot": res = matchNot(json); break;
      case "exitMatching": res = exitMatching(json); break;
      case "setStep": res = setStep(json); break;
      default: break;
    }
    this.match.broadCast(res);
    while (!this.threadQueue.isEmpty()) {
      Thread thread = threadQueue.poll();
      es.execute(thread);
    }
  }

  /**
   * 加入待办
   *
   * @param thread
   */
  void addAbeyance(Thread thread) {
    threadQueue.add(thread);
  }

  /**
   * 设置步
   *
   * @param json
   * @return
   */
  private Res setStep(JSONObject json) {
    JSONObject step = json.getJSONObject("step");
    this.match.getGame().setStep(step);
    return Res.ok("");
  }

  /**
   * 退出匹配
   *
   * @param json
   * @return
   */
  private Res exitMatching(JSONObject json) {
    this.match.someoneExit(this);
    return Res.ok("");
  }

  /**
   * 取消准备
   *
   * @param json
   * @return
   */
  private Res matchNot(JSONObject json) {
    this.match.setOk(this, false);
    json.set("id", match.getMe(this));
    return Res.ok(json);
  }

  /**
   * 准备好
   *
   * @param json
   * @return
   */
  private Res matchOk(JSONObject json) {
    this.match.setOk(this, true);
    json.set("id", match.getMe(this));
    if (this.match.allOk())
      new Thread(() -> {
        this.startMulitGaming();
      }).start();
    return Res.ok(json);
  }

  /**
   * 取消匹配
   *
   * @param json
   * @return
   */
  private Res cancelMatching(JSONObject json) {
    this.bot = null;
    this.removeFromMatch();
    return Res.ok(json);
  }

  /**
   * 加入匹配池
   *
   * @param json
   * @return
   */
  Res startMatching(JSONObject json) {
    // 开始匹配的时候必须初始化match，否则某个对局结束后会相互影响
    this.initMatch();

    Integer botId = json.getInt("botId");
    if (botId == 0) this.bot = null;
    else this.bot = new RunningBot(botId);
    this.addToMatch();
    return Res.ok(json);
  }

  /**
   * 查找机器人
   *
   * @param id
   * @return
   */
  public boolean findBot(Integer id) {
    return BotDAO.selectOne(new QueryWrapper<Bot>().eq("id", id).eq("game_id", 1)) != null;
  }

  /**
   * 开始单人游戏
   *
   * @param json
   * @return
   */
  Res startSingleGaming(JSONObject json) {
    // 获取机器人编号
    int i = 0;
    List<Integer> botIds = json.getBeanList("botIds", Integer.class);
    List<RunningBot> bots = new ArrayList<>();
    for (Integer botId : botIds) {
      if (botId > 0) {
        if (!this.findBot(botId)) return Res.fail(String.format("不存在%d号Bot", botId));
        else bots.add(new RunningBot(botId));
      } else bots.add(null);
    }

    // 设置玩家
    this.initMatch();

    // 创建游戏
    AbsGame game = createGameObject("single", match, bots);
    game.setMatch(this.match);
    this.match.setGame(game);

    // 获取初始数据
    JSONObject initData = game.getInitDataAndSave();
    json.set("initData", initData);

    // 启动所有机器人
    game.compileBots();

    addAbeyance(new Thread(() -> {
      lock.lock();
      game.start();
      lock.unlock();
    }));
    return Res.ok(json);
  }

  /**
   * 发送消息
   *
   * @param json
   * @return
   */
  Res sendTalk(JSONObject json) {
    json.set("userId", this.user.getId());
    json.set("username", this.user.getUsername());
    return Res.ok(json);
  }

  /**
   * 加入到匹配池
   */
  public void addToMatch() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "snake");
    data.add("userId", user.getId().toString());
    data.add("rating", SnakeRatingDAO.selectById(user.getId()).getRating().toString());
    RT.POST(addUrl, data);
  }

  /**
   * 从匹配池中删除
   */
  public void removeFromMatch() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", "snake");
    data.add("userId", user.getId().toString());
    RT.POST(removeUrl, data);
  }

  /**
   * 开始多人游戏
   */
  void startMulitGaming() {
    JSONObject json = new JSONObject();
    json.set("action", "startMultiGaming");

    AbsGame game = createGameObject("multi", this.match, this.match.getBots());
    game.setMatch(this.match);
    this.match.setGame(game);

    // 获取初始数据
    JSONObject initData = game.getInitDataAndSave();
    json.set("initData", initData);

    // 获取botIds
    List<RunningBot> bots = this.match.getBots();
    List<Integer> ids = new ArrayList<>();
    for (RunningBot runningBot : bots) {
      if (runningBot != null) ids.add(runningBot.getBot().getId());
      else ids.add(0);
    }
    json.set("botIds", ids);

    // 启动机器人
    game.compileBots();


    this.match.broadCast(Res.ok(json));

    // 开始游戏
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    game.start();
  }
}
