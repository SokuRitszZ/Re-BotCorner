package com.soku.rebotcorner.service.bot.record;

import com.soku.rebotcorner.pojo.Record;

import java.util.List;

public interface GetListByGameIdService {
  List<Record> getListByGameId(Integer gameId);
}
