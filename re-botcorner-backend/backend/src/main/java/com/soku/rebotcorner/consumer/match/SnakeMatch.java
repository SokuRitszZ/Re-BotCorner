package com.soku.rebotcorner.consumer.match;

import com.soku.rebotcorner.consumer.SnakeWebSocketServer;

public class SnakeMatch {
  public SnakeWebSocketServer[] sockets;
  public boolean[] isOk;

  public SnakeMatch(SnakeWebSocketServer socket0, SnakeWebSocketServer socket1) {
    sockets = new SnakeWebSocketServer[2];
    sockets[0] = socket0;
    sockets[1] = socket1;
    isOk = new boolean[2];
    isOk[0] = isOk[1] = false;
  }

  public boolean allOk() {
    return isOk[0] && isOk[1];
  }
}
