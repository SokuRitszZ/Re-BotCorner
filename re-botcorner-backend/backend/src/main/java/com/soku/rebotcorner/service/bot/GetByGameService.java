package com.soku.rebotcorner.service.bot;

import com.soku.rebotcorner.pojo.Bot;

import java.util.List;
import java.util.Map;

public interface GetByGameService {
  List<Bot> getByGame(Map<String, String> data);
}
