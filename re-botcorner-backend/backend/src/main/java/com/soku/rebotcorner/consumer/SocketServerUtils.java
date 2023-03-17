package com.soku.rebotcorner.consumer;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.consumer.match.GameMatch;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.utils.UserDAO;

import java.util.List;

public class SocketServerUtils {
  public static JSONObject packUser(User user) {
    JSONObject json = new JSONObject()
      .set("id", user.getId())
      .set("username", user.getUsername())
      .set("avatar", user.getAvatar());
    return json;
  }

  public static JSONObject packMatch(GameMatch match) {
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
}
