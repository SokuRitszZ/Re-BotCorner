package com.soku.rebotcorner.service.bot.record;

import cn.hutool.json.JSONObject;

import java.util.List;

public interface RecordService {
  JSONObject getBaseRecordByGameId(Integer gameId, Integer from, Integer count);

  JSONObject getRecordCount(Integer gameId);

  JSONObject getRecordJson(Integer id);
}
