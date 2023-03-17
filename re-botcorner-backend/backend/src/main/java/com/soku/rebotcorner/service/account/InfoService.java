package com.soku.rebotcorner.service.account;

import cn.hutool.json.JSONObject;

public interface InfoService {
  JSONObject getInfo();

  JSONObject getInfoById(Integer id);
}
