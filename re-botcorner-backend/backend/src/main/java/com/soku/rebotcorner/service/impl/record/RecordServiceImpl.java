package com.soku.rebotcorner.service.impl.record;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.bot.record.RecordService;
import com.soku.rebotcorner.utils.BotDAO;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.RecordDAO;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecordServiceImpl implements RecordService {
  public JSONObject getBaseRecordByGameId(Integer gameId, Integer from, Integer count) {
    List<JSONObject> list = RecordDAO.mapper.getBaseRecordByGameId(gameId, from, count);
    Map<String, JSONObject>
      userMap = new HashMap<>(),
      botMap = new HashMap<>();

    // 一个 Record
    for (JSONObject json : list) {
      String[] botIds = json.getStr("botIds").split(",");
      String[] userIds = json.getStr("userIds").split(",");
      String[] titles = new String[2];
      JSONObject[] users = new JSONObject[2];

      for (int i = 0; i < botIds.length; i++) {
        String botId = botIds[i];
        String userId = userIds[i];

        JSONObject bot, user;

        if (botId != "0")  {
          if (!botMap.containsKey(botId)) botMap.put(botId, BotDAO.mapper.getBaseBotById(Integer.valueOf(botId)));
          bot = botMap.get(botId);
        } else bot = null;
        // user
        if (!userMap.containsKey(userId)) {
          userMap.put(userId, UserDAO.mapper.getBaseById(Integer.valueOf(userId)));
        }
        user = userMap.get(userId);

        if (bot != null) {
          titles[i] = String.format("%s[%s]", user.getStr("username"), bot.getStr("title"));
        } else {
          titles[i] = user.getStr("username");
        }
        users[i] = user;
      }

      json.remove("userIds");
      json.remove("botIds");
      json
        .set("titles", titles)
        .set("users", users);
    }

    return NewRes.ok(
      new JSONObject()
        .set("records", list)
    );
  }

  @Override
  public JSONObject getRecordCount(Integer gameId) {
    Long count = RecordDAO.mapper.selectCount(new QueryWrapper<Record>().eq("game_id", gameId));
    return NewRes.ok(new JSONObject()
      .set("count", count)
    );
  }

  @Override
  public JSONObject getRecordJson(Integer id) {
    return NewRes.ok(new JSONObject( RecordDAO.mapper.getRecordJson(id)));
  }
}
