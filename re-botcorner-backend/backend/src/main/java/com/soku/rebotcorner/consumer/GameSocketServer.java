package com.soku.rebotcorner.consumer;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.games.*;
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

@Data
@Component
@ServerEndpoint("/websocket/{game}/{token}")  // 注意不要以'/'结尾
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

  private Queue<Thread> threadQueue = new LinkedList<>();

  public static void makeMatching(List<Integer> userIds) {
    // 获取基本信息
    List<JSONObject> jsons = new ArrayList<>();
    // 获取socket
    List<GameSocketServer> sockets = new ArrayList<>();
    for (Integer userId : userIds) {
      JSONObject user = UserDAO.mapper.getBaseById(userId);
      jsons.add(user);

      GameSocketServer socket = users.get(userId);
      sockets.add(socket);
    }
    // 生成Matching
    GameMatch match = new GameMatch(sockets);

    // 广播
    JSONObject ret = new JSONObject();
    JSONObject data = new JSONObject();
    data.set("userData", jsons);
    ret.set("action", "make match");
    ret.set("data", data);
    match.broadCast(ret);
  }

  public void setMatch(GameMatch match) {
    this.match = match;
  }

  AbsGame createGameObject(String mode, GameMatch match, List<RunningBot> bots) {
    switch (this.gameClass) {
      case "hex":
        return new HexGame(mode, match, bots);
      case "snake":
        return new SnakeGame(mode, match, bots);
      case "reversi":
        return new ReversiGame(mode, match, bots);
      case "backgammon":
        return new BackgammonGame(mode, match, bots);
    }
    return null;
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("game") String game, @PathParam("token") String token) {
    this.session = session;
    Integer userId = JwtAuthenticationUtil.getUserId(token);

    this.initMatch();

    System.out.println(String.format("%d connected.", userId));
    this.user = UserDAO.mapper.selectById(userId);
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
      } else {
        this.match.someoneExit(this);
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
    JSONObject json = new JSONObject(message);
    this.route(json);
  }

  public void sendMessage(JSONObject json) {
    if (hasClosed) return;
    synchronized (this.session) {
      try {
        this.session.getBasicRemote().sendText(json.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void initMatch() {
    ArrayList<GameSocketServer> sockets = new ArrayList<>();
    sockets.add(this);
    this.match = new GameMatch(sockets);
  }

  private void route(JSONObject json) {
    String action = json.getStr("action");
    System.out.println(this.user.getId() + " action = " + action);
    JSONObject res = null;
    switch (action) {
      case "start single game":
        res = startSingleGaming(json.getJSONObject("data"));
        break;
//      case "send talk": res = sendTalk(json); break;
      case "start match":
        res = startMatching(json);
        break;
      case "exit match":
        res = exitMatching(json);
        break;
      case "toggle match":
        res = toggleMatch(json);
        break;
      case "set step":
        res = setStep(json);
        break;
      case "start game":
        res = startGame();
        break;
      default:
        break;
    }
    this.match.broadCast(res);
    while (!this.threadQueue.isEmpty()) {
      Thread thread = threadQueue.poll();
      es.execute(thread);
    }
  }

  private JSONObject toggleMatch(JSONObject json) {
    boolean isOk = json.getJSONObject("data").getBool("isOk");
    this.match.setOk(this, isOk);
    if (this.match.allOk())
      new Thread(() -> {
        this.startMultiGaming();
      }).start();
    return new JSONObject()
      .set("action", "toggle match")
      .set("data", new JSONObject()
        .set("id", this.match.getMe(this))
        .set("isOk", isOk)
      );
  }

  /**
   * 设置步
   *
   * @param json
   * @return
   */
  private JSONObject setStep(JSONObject json) {
    JSONObject ret = new JSONObject();
    ret.set("action", "nothing");

    if (this.match.getGame() != null)
      this.match.getGame().setStep(json.getJSONObject("data"));

    return ret;
  }

  /**
   * 退出匹配
   *
   * @param json
   * @return
   */
  private JSONObject exitMatching(JSONObject json) {
    if (this.match.getSockets().size() == 1) {
      this.bot = null;
      this.removeFromMatch();
    } else {
      this.match.someoneExit(this);
    }
    return new JSONObject()
      .set("action", "nothing");
  }

  /**
   * 加入匹配池
   *
   * @param json
   * @return
   */
  JSONObject startMatching(JSONObject json) {
    // 开始匹配的时候必须初始化match，否则某个对局结束后会相互影响
    this.initMatch();
    Integer botId = json
      .getJSONObject("data")
      .getInt("botId");
    if (botId == 0) this.bot = null;
    else this.bot = new RunningBot(botId);
    this.addToMatch();
    return new JSONObject().set("action", "start match");
  }

  /**
   * 查找机器人
   *
   * @param id
   * @return
   */
  public boolean findBot(Integer id) {
    return BotDAO.selectOne(new QueryWrapper<Bot>().eq("id", id).eq("game_id", this.getGameId())) != null;
  }

  /**
   * 获取游戏Id
   *
   * @return
   */
  public int getGameId() {
    switch (this.gameClass) {
      case "snake":
        return 1;
      case "reversi":
        return 2;
      case "backgammon":
        return 3;
      case "hex":
        return 4;
    }
    return 0;
  }

  /**
   * 开始单人游戏
   *
   * @param json
   * @return
   */
  JSONObject startSingleGaming(JSONObject json) {
    JSONObject ret = new JSONObject();
    ret.set("action", "start single game");

    JSONObject data = new JSONObject();
    // 获取机器人编号
    List<Integer> botIds = json.getBeanList("botIds", Integer.class);
    data.set("botIds", botIds);

    List<RunningBot> bots = new ArrayList<>();
    for (Integer botId : botIds) {
      if (botId > 0) {
        if (!this.findBot(botId)) {
          data.set("error", "不存在此编号的Bot");
          ret.set("data", data);
          return ret;
        } else bots.add(new RunningBot(botId));
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
    data.set("initData", initData);

    // 返回数据
    this.match.broadCast(ret
      .set("data", data
        .set("mode", "single"))
    );

    // 启动所有机器人
    game.compileBots();

    return new JSONObject()
      .set("action", "allow to control");
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
    data.add("game", this.gameClass);
    data.add("userId", user.getId().toString());
    data.add("rating", SnakeRatingDAO.selectById(user.getId()).getRating().toString());
    RT.POST(addUrl, data);
  }

  /**
   * 从匹配池中删除
   */
  public void removeFromMatch() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("game", this.gameClass);
    data.add("userId", user.getId().toString());
    RT.POST(removeUrl, data);
  }

  /**
   * 开始多人游戏
   */
  void startMultiGaming() {
    AbsGame game = createGameObject("multi", this.match, this.match.getBots());
    game.setMatch(this.match);
    this.match.setGame(game);

    // 获取初始数据
    JSONObject initData = game.getInitDataAndSave();

    // 获取botIds
    List<RunningBot> bots = this.match.getBots();
    List<Integer> ids = new ArrayList<>();
    for (RunningBot runningBot : bots) {
      if (runningBot != null) ids.add(runningBot.getBot().getId());
      else ids.add(0);
    }

    this.match.broadCast(new JSONObject()
      .set("action", "start multi game")
      .set("data", new JSONObject()
        .set("mode", "multi")
        .set("initData", initData)
        .set("botIds", ids))
    );

    // 启动机器人
    game.compileBots();

    this.match.broadCast(new JSONObject()
      .set("action", "allow to control")
    );
  }

  /**
   * 前端都准备好才能开始
   */
  JSONObject startGame() {
    this.match.setStartGame(this);
    this.sendMessage(new JSONObject()
      .set("action", "start game")
    );
    return new JSONObject()
      .set("action", "nothing");
  }
}
