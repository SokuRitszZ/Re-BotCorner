package com.soku.rebotcorner.service.account.acwing;

import cn.hutool.json.JSONObject;

public interface WebService {
  JSONObject applyCode();

  JSONObject receiveCode(String code, String state);
}
