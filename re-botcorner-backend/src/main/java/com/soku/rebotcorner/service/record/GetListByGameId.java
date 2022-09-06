package com.soku.rebotcorner.service.record;

import com.soku.rebotcorner.pojo.Record;

import java.util.List;
import java.util.Map;

public interface GetListByGameId {
  List<Map<String, String>> get(Map<String, String> data);
}
