package com.soku.rebotcorner.utils;

import cn.hutool.json.JSONUtil;

import java.util.HashMap;

public class Res extends HashMap<String, String> {
  public static Res ok(String message) {
    Res res = new Res();
    res.put("result", "success");
    res.put("message", message);
    return res;
  }

  public static Res ok(Object object) {
    Res res = new Res();
    res.put("result", "success");
    if (object != null) res.put("data", JSONUtil.toJsonStr(object));
    return res;
  }

  public static Res fail(String message) {
    Res res = new Res();
    res.put("result", "fail");
    res.put("message", message);
    return res;
  }
}
