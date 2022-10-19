package com.soku.rebotcorner.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.pojo.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseRecord {
  public static JSONObject parseJSON(Record record) {
    JSONObject returnJson = JSONUtil.parseObj(record);
    JSONObject json = JSONUtil.parseObj(record.getJson());

    returnJson.set("result", json.getStr("result"));

    List<Integer> userIds = json.getBeanList("userIds", Integer.class);
    List<Integer> botIds = json.getBeanList("botIds", Integer.class);
    json.remove("userIds");
    json.remove("botIds");
    List<JSONObject> infos = new ArrayList<>();
    while (userIds.size() != botIds.size()) userIds.add(userIds.get(userIds.size() - 1));

    Map<Integer, User> userMap = new HashMap<>();
    Map<Integer, Bot> botMap = new HashMap<>();
    for (int i = 0; i < userIds.size(); ++i) {
      Integer userId = userIds.get(i);
      if (!userMap.containsKey(userId)) userMap.put(userId, UserDAO.selectById(userId));
      User user = userMap.get(userId);

      Integer botId = botIds.get(i);
      if (!botMap.containsKey(botId)) botMap.put(botId, BotDAO.selectById(botId));

      Bot bot = botId == 0 ? null : botMap.get(botId);

      JSONObject info = new JSONObject();

      // 用户id，用户名，头像
      userId = user.getId();
      String username = user.getUsername();
      String headIcon = user.getHeadIcon();
      if (bot != null) username = String.format("%s[%s]", username, bot.getTitle());
      info.set("userId", userId);
      info.set("username", username);
      info.set("headIcon", headIcon);
      infos.add(info);
    }
    returnJson.set("infos", infos);

    return returnJson;
  }
}
