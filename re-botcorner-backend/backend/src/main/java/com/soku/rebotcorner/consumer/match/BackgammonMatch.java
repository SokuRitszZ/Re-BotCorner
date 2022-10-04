package com.soku.rebotcorner.consumer.match;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.consumer.BackgammonWebSocketServer;

public class BackgammonMatch {
  public BackgammonWebSocketServer[] sockets;
  public boolean[] isOk;

  public BackgammonMatch(BackgammonWebSocketServer socket0, BackgammonWebSocketServer socket1) {
    sockets = new BackgammonWebSocketServer[]{socket0, socket1};
    isOk = new boolean[2];
    isOk[0] = isOk[1] = false;
  }

  public void broadcast(JSONObject json) {
    if (sockets[0] != null) sockets[0].sendMessage(json);
    if (sockets[0] != sockets[1] && sockets[1] != null) sockets[1].sendMessage(json);
  }

  public boolean isAllOk() {
    return isOk[0] && isOk[1];
  }

  public int getMe(BackgammonWebSocketServer socket) {
    return sockets[0] == socket ? 0 : 1;
  }
}
