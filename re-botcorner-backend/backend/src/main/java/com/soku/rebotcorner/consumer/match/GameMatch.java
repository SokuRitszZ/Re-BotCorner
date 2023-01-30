package com.soku.rebotcorner.consumer.match;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.GameSocketServer;
import com.soku.rebotcorner.games.AbsGame;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.Res;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GameMatch {
  private UUID uuid = UUID.randomUUID();
  private List<GameSocketServer> sockets;
  private ConcurrentHashMap<Integer, GameSocketServer> watchers = new ConcurrentHashMap<>();
  private AbsGame game;
  private boolean[] isOk;
  private boolean[] canStartGame;
  private ReentrantLock lock;
  private ConcurrentHashMap<UUID, GameMatch> belong;

  /**
   * 构造函数
   *
   * @param sockets
   */
  public GameMatch(List<GameSocketServer> sockets) {
    this.sockets = sockets;
    this.isOk = new boolean[sockets.size()];
    this.canStartGame = new boolean[sockets.size()];
    for (int i = 0; i < this.canStartGame.length; i++) {
      this.canStartGame[i] = false;
    }
    for (GameSocketServer socket : sockets)
      if (socket != null)
        socket.setMatch(this);
    this.lock = new ReentrantLock();
  }

  /**
   * 广播消息
   *
   * @param res
   */
  public void broadCast(JSONObject res) {
    Set<GameSocketServer> set = new HashSet<>();
    for (GameSocketServer socket : sockets) {
      if (socket != null && !set.contains(socket)) {
        set.add(socket);
        socket.sendMessage(res);
      }
    }
    for (GameSocketServer watcher : watchers.values()) {
      watcher.sendMessage(res);
    }
  }

  /**
   * 获取自己在游戏中的编号
   *
   * @param socket
   * @return
   */
  public Integer getMe(GameSocketServer socket) {
    for (int i = 0; i < sockets.size(); ++i) {
      if (sockets.get(i) == socket) return i;
    }
    return -1;
  }

  /**
   * 设置准备状态
   *
   * @param socket
   * @param ok
   */
  public void setOk(GameSocketServer socket, boolean ok) {
    Integer index = getMe(socket);
    isOk[index] = ok;
  }

  /**
   * 检查是否全部准备好了
   *
   * @return
   */
  public boolean allOk() {
    for (boolean b : isOk) if (!b) return false;
    return true;
  }

  /**
   * 踢出所有人
   */
  public void someoneExit(GameSocketServer socket) {
    Integer theOne = this.getMe(socket);
    JSONObject json = new JSONObject();
    json.set("action", "exit match");
    json.set("data", new JSONObject().set("id", theOne));
    this.broadCast(json);
    for (GameSocketServer gameSocket : this.sockets) {
      gameSocket.initMatch();
      if (socket != gameSocket) gameSocket.addToMatch();
    }
  }

  /**
   * 获取所有玩家的Bot
   *
   * @return
   */
  public List<RunningBot> getBots() {
    List<RunningBot> list = new ArrayList<>();
    for (GameSocketServer socket : this.sockets)
      list.add(socket.getBot());
    return list;
  }

  /**
   * 只发给特定的人
   *
   * @param id
   * @param res
   */
  public void sendOne(int id, Res res) {
    if (id >= this.sockets.size()) {
      for (int i = 0; i < this.sockets.size(); ++i)
        if (this.sockets.get(i) != null) {
          id = i;
          break;
        }
    }
    this.sockets.get(id).sendMessage(JSONUtil.parseObj(res));
  }

  public void setStartGame(GameSocketServer socket) {
    lock.lock();
    int me = getMe(socket);
    canStartGame[me] = true;
    if (isAllReady()) {
      this.game.start();
    }
    lock.unlock();
  }

  public boolean isAllReady() {
    for (boolean b : canStartGame)
      if (!b)
        return false;
    return true;
  }

  public List<GameSocketServer> getSockets() {
    return this.sockets;
  }

  public AbsGame getGame() {
    return this.game;
  }

  public void setGame(AbsGame game) {
    this.game = game;
  }

  public void setBelong(ConcurrentHashMap<UUID, GameMatch> belong) {
    this.belong = belong;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void gameOver() {
    this.belong.remove(this.uuid);

    GameSocketServer.allBroadCast(this.game.getGameId(), new JSONObject()
      .set("action", "one game over")
      .set("data", new JSONObject()
        .set("uuid", this.uuid))
    );
  }

  public void addWatcher(GameSocketServer socket) {
    watchers.put(socket.getUser().getId(), socket);
  }

  public void delWatcher(GameSocketServer socket) {
    watchers.remove(socket.getUser().getId());
  }
}
