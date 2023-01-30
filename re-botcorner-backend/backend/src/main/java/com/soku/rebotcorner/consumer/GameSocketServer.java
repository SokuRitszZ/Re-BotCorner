package com.soku.rebotcorner.consumer;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.games.*;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.Rating;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
@ServerEndpoint("/websocket/{game}/{token}")  // 注意不要以'/'结尾
public class GameSocketServer {
  private final static String addUrl = "http://localhost:8081/api/matching/add/";
  private final static String removeUrl = "http://localhost:8081/api/matching/remove/";

  private static ConcurrentHashMap<Integer, GameSocketServer> users = new ConcurrentHashMap<>();
  private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, GameSocketServer>> game2users = new ConcurrentHashMap<>();
  private static ConcurrentHashMap<Integer, ConcurrentHashMap<UUID, GameMatch>> game2matches = new ConcurrentHashMap<>();

  private Session session;
  private User user;
  private boolean hasClosed;
  private GameMatch matchWatching = null;
  private GameMatch match;
  private String gameClass;
  private RunningBot bot;

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

  public static void allBroadCast(Integer gameId, JSONObject json) {
    if (!game2users.containsKey(gameId)) {
      game2users.put(gameId, new ConcurrentHashMap<>());
    }
    for (GameSocketServer value : game2users.get(gameId).values()) {
      value.sendMessage(json);
    }
  }

  private static JSONObject packUser(User user) {
    JSONObject json = new JSONObject()
      .set("id", user.getId())
      .set("username", user.getUsername())
      .set("avatar", user.getAvatar());
    return json;
  }

  private static JSONObject packMatch(GameMatch match) {
    List<GameSocketServer> sockets = match.getSockets();
    Object[] users = sockets.stream().map(s -> packUser(s.getUser())).toArray();

    Object[] bots = match.getGame().getBots().stream()
      .map(b -> {
        if (b == null) return null;
        else {
          JSONObject pack = b.pack();
          return pack.set("user", UserDAO.mapper.getBaseById(pack.getInt("user")));
        }
      }).toArray();

    JSONObject json = new JSONObject()
      .set("uuid", match.getUuid())
      .set("users", users)
      .set("bots", bots);
    return json;
  }

  public void setMatch(GameMatch match) {
    this.match = match;
  }

  AbsGame createGameObject(String mode, GameMatch match, List<RunningBot> bots) {
    switch (this.gameClass) {
      case "gomoku":
        return new GomokuGame(mode, match, bots);
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
    this.user = UserDAO.mapper.selectById(userId);
    this.gameClass = game;
    this.initMatch();

    // 广播有人加入了游戏（不包括自己）
    allBroadCast(getGameId(),
      new JSONObject()
        .set("action", "join")
        .set("data", packUser(user))
    );

    users.put(userId, this);
    game2users.get(getGameId()).put(userId, this);

    Object[] onlineUsers = game2users.get(getGameId()).values()
      .stream()
      .map(server -> packUser(server.getUser())).toArray();

    if (!game2matches.containsKey(getGameId()))
      game2matches.put(getGameId(), new ConcurrentHashMap<>());
    Object[] matches = game2matches.get(getGameId()).values().stream()
      .map(m -> packMatch(m)).toArray();

    sendMessage(
      new JSONObject()
        .set("action", "init")
        .set("data", new JSONObject()
          .set("users", onlineUsers)
          .set("matches", matches)
        )
    );
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
      users.remove(user.getId());
      game2users.get(getGameId()).remove(user.getId());
      removeFromMatch();
    }
    allBroadCast(getGameId(),
      new JSONObject()
        .set("action", "leave")
        .set("data", new JSONObject()
          .set("id", user.getId())
        )
    );
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
    JSONObject res = null;
    switch (action) {
      case "start single game":
        res = startSingleGaming(json.getJSONObject("data"));
        break;
      case "send talk":
        res = sendTalk(json);
        break;
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
      case "to watch":
        res = toWatch(json.getJSONObject("data"));
        break;
      default:
        break;
    }
    if (res != null) this.match.broadCast(res);
  }

  private JSONObject toggleMatch(JSONObject json) {
    boolean isOk = json.getJSONObject("data").getBool("isOk");
    this.match.setOk(this, isOk);
    this.match.broadCast(
      new JSONObject()
        .set("action", "toggle match")
        .set("data", new JSONObject()
          .set("id", this.match.getMe(this))
          .set("isOk", isOk)
        )
    );
    if (this.match.allOk())
      this.startMultiGaming();
    return null;
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
    try {
      Bot bot = BotDAO.mapper.selectOne(
        new QueryWrapper<Bot>()
          .eq("id", id)
          .eq("game_id", getGameId())
          .eq("visible", true)
      );
      if (bot != null) return true;
      Integer userId = getUser().getId();
      bot = BotDAO.mapper.selectOne(
        new QueryWrapper<Bot>()
          .eq("id", id)
          .eq("game_id", getGameId())
          .eq("user_id", userId)
      );
      return bot != null;
    } catch (Exception e) {
      return false;
    }
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
      case "gomoku":
        return 5;
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
    beforeStartGame();

    JSONObject ret = new JSONObject();
    ret.set("action", "start single game");

    JSONObject data = new JSONObject();
    // 获取机器人编号
    List<Integer> botIds = json.getBeanList("botIds", Integer.class);
    data.set("botIds", botIds);

    List<RunningBot> bots = new ArrayList<>();
    for (Integer botId : botIds) {
      if (botId > 0) {
        if (!findBot(botId)) {
          data.set("error", String.format("不存在编号为#%d的Bot", botId));
          ret.set("data", data);
          return ret;
        } else bots.add(new RunningBot(botId));
      } else bots.add(null);
    }

    // 设置玩家
    this.initMatch();

    // 创建游戏
    AbsGame game = createGameObject("single", match, bots).setStarterId(user.getId());
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

    this.match.broadCast(new JSONObject()
      .set("action", "allow to control")
    );

    // 通知所有在房间里的人，有新游戏开了
    allBroadCast(getGameId(), new JSONObject()
      .set("action", "one game start")
      .set("data", packMatch(this.match))
    );
    // 记录正在进行的比赛
    if (!game2matches.containsKey(getGameId()))
      game2matches.put(getGameId(), new ConcurrentHashMap<>());

    ConcurrentHashMap<UUID, GameMatch> gameMatches = game2matches.get(getGameId());
    gameMatches.put(this.match.getUuid(), this.match);
    this.match.setBelong(gameMatches);

    return null;
  }

  /**
   * 发送消息
   *
   * @param json
   * @return
   */
  JSONObject sendTalk(JSONObject json) {
    return new JSONObject()
      .set("action", "send talk")
      .set("data",
        json.getJSONObject("data")
          .set("time", new Date()));
  }

  /**
   * 加入到匹配池
   */
  public void addToMatch() {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    Rating rating = RatingDAO.mapper.selectOne(new QueryWrapper<Rating>().eq("game_id", getGameId()).eq("user_id", user.getId()));
    if (rating == null) {
      rating = new Rating(user.getId(), getGameId(), 1500);
      RatingDAO.mapper.insert(rating);
    }
    data.add("game", this.gameClass);
    data.add("userId", user.getId().toString());
    data.add("rating", rating.getScore().toString());
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
    beforeStartGame();

    AbsGame game = createGameObject("multi", this.match, this.match.getBots()).setStarterId(user.getId());
    game.setMatch(this.match);
    this.match.setGame(game);

    // 获取初始数据
    JSONObject initData = game.getInitDataAndSave();

    // 获取botIds
    List<RunningBot> bots = this.match.getGame().getBots();
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

    // 通知所有在房间里的人，有新游戏开了
    allBroadCast(getGameId(), new JSONObject()
      .set("action", "one game start")
      .set("data", packMatch(this.match))
    );
    // 记录正在进行的比赛
    if (!game2matches.containsKey(getGameId()))
      game2matches.put(getGameId(), new ConcurrentHashMap<>());
    ConcurrentHashMap<UUID, GameMatch> gameMatches = game2matches.get(getGameId());
    gameMatches.put(this.match.getUuid(), this.match);
    this.match.setBelong(gameMatches);
  }

  void beforeStartGame() {
    toWatch((GameMatch) null);
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

  JSONObject toWatch(JSONObject json) {
    String strUuid = json.getStr("uuid");
    UUID uuid = UUID.fromString(strUuid);
    GameMatch match = game2matches.get(getGameId()).get(uuid);

    JSONObject current;
    if (match != null) current = match.getGame().getCurrent();
    else current = null;

    toWatch(match);
    return new JSONObject()
      .set("action", "get current")
      .set("data", current);
  }

  public void toWatch(GameMatch match) {
    if (matchWatching != null) {
      matchWatching.delWatcher(this);
      matchWatching = null;
    }
    if (match != null) match.addWatcher(this);
    matchWatching = match;
  }
}
