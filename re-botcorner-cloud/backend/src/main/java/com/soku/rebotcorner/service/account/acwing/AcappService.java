package com.soku.rebotcorner.service.account.acwing;

import com.alibaba.fastjson.JSONObject;

public interface AcappService {
  JSONObject applyCode();
  JSONObject receiveCode(String code, String state);
}
