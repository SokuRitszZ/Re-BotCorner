package com.soku.rebotcorner.utils;

import cn.hutool.json.JSONObject;

public class NewRes {
  public static JSONObject ok(JSONObject json) {
    JSONObject ret = new JSONObject();
    ret.set("result", "success");
    ret.set("data", json);
    return ret;
  }

  public static JSONObject fail(String message) {
    JSONObject ret = new JSONObject();
    ret.set("result", "fail");
    ret.set("message", message);
    return ret;
  }
}
