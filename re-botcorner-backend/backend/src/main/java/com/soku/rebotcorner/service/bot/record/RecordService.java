package com.soku.rebotcorner.service.bot.record;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.Record;

import java.util.List;

public interface RecordService {
  List<JSONObject> getBaseRecordByGameId(Integer gameId);
}
