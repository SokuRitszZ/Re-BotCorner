package com.soku.rebotcorner.service.record;

import cn.hutool.json.JSONObject;

public interface RecordService {
  JSONObject getBaseRecordByGameId(Integer gameId, Integer from, Integer count);

  JSONObject getRecordCount(Integer gameId);

  JSONObject getRecordJson(Integer id);

  JSONObject getTopRecord();
}
