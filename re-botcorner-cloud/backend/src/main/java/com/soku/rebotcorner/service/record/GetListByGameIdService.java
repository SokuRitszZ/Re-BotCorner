package com.soku.rebotcorner.service.record;

import com.soku.rebotcorner.pojo.Record;

import java.util.List;
import java.util.Map;

public interface GetListByGameIdService {
  List<Record> getListByGameId(Map<String, String> data);
}
