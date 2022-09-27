package com.soku.rebotcorner.service.account.acwing;

import com.alibaba.fastjson.JSONObject;

public interface WebService {
  JSONObject applyCode();
  JSONObject receiveCode(String code, String state);
}
