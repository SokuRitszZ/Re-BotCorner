package com.soku.rebotcorner.service.account;

import cn.hutool.json.JSONObject;

public interface RegisterService {
  JSONObject register(String username, String password, String confirmedPassword);
}
