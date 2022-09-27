package com.soku.rebotcorner.consumer.match;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.consumer.ReversiWebSocketServer;

public class ReversiMatch {
  public ReversiWebSocketServer[] sockets;
  public boolean[] isOk;

  public ReversiMatch(ReversiWebSocketServer sockets[]) {
    this.sockets = sockets;
    isOk = new boolean[2];
    isOk[0] = isOk[1] = false;
  }

  public void broadcast(JSONObject json) {
    sockets[0].sendMessage(json);
    if (sockets[0] != sockets[1]) sockets[1].sendMessage(json);
  }

  public boolean isAllOk() {
    return isOk[0] && isOk[1];
  }

  public int getMe(ReversiWebSocketServer socket) {
    return sockets[1] == socket ? 1 : 0;
  }
}
