package com.soku.rebotcorner.service.account;

import cn.hutool.json.JSONObject;

public interface UpdateProfileService {
  JSONObject solve(String username, String password, String signature);
}
