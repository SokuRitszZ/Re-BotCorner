package com.soku.rebotcorner.consumer.match;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.consumer.GameSocketServer;
import com.soku.rebotcorner.games.AbsGame;
import com.soku.rebotcorner.utils.Res;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class GameMatch {
  private List<GameSocketServer> sockets;
  private AbsGame game;
  private boolean[] isOk;

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
   * 获取自己属于0还是1
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
}
