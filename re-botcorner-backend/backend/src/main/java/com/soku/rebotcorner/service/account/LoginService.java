package com.soku.rebotcorner.service.account;

import cn.hutool.json.JSONObject;

public interface LoginService {
  JSONObject getToken(String username, String password);
}
