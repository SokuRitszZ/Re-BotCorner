package com.soku.rebotcorner.service.bot;

import cn.hutool.json.JSONObject;

public interface BotService {
  JSONObject addBot(JSONObject data);

  JSONObject deleteBot(Integer id);

  JSONObject getAll();

  JSONObject getCode(int id);

  JSONObject updateBot(JSONObject data);

  JSONObject changeVisible(int id, boolean visible);
}