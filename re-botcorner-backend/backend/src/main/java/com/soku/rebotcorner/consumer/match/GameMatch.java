package com.soku.rebotcorner.consumer.match;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.GameSocketServer;
import com.soku.rebotcorner.games.AbsGame;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.utils.Res;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameMatch {
  private List<GameSocketServer> sockets;
  private AbsGame game;
  private boolean[] isOk;

  /**
   * game getter
   *
   * @return
   */
  public AbsGame getGame() {
    return game;
  }

  /**
   * game setter
   *
   * @param game
   */
  public void setGame(AbsGame game) {
    this.game = game;
  }

  /**
   * 构造函数
   *
   * @param sockets
   */
  public GameMatch(List<GameSocketServer> sockets) {
    this.sockets = sockets;
    isOk = new boolean[sockets.size()];
    for (GameSocketServer socket : sockets) {
      if (socket != null) {
        socket.setMatch(this);
      }
    }
  }

  /**
   * 广播消息
   *
   * @param res
   */
  public void broadCast(Res res) {
    JSONObject json = JSONUtil.parseObj(res);
    Set<GameSocketServer> set = new HashSet<>();
    for (GameSocketServer socket : sockets) {
      if (socket != null && !set.contains(socket)) {
        set.add(socket);
        socket.sendMessage(json);
      }
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
    json.set("action", "exitMatching");
    json.set("id", theOne);
    this.broadCast(Res.ok(json));
    for (GameSocketServer gameSocket : this.sockets) {
      gameSocket.initMatch();
      if (socket != gameSocket) gameSocket.addToMatch();
    }
  }

  /**
   * 获取所有userId
   * @return
   */
  public List<Integer> getUserIds() {
    List<Integer> list = new ArrayList<>();
    for (GameSocketServer socket : this.sockets)
      if (socket != null)
        list.add(socket.getUser().getId());
      else
        list.add(list.get(list.size() - 1));
    return list;
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
}
