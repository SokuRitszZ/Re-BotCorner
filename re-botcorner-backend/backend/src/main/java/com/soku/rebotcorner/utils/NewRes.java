package com.soku.rebotcorner.utils;

import cn.hutool.json.JSONObject;

public class NewRes {
  /**
   * 成功的同时提供数据
   *
   * @param json
   * @return
   */
  public static JSONObject ok(JSONObject json) {
    JSONObject ret = new JSONObject();
    ret.set("result", "success");
    ret.set("data", json);
    return ret;
  }

  /**
   * 仅仅作为成功提示
   *
   * @return
   */
  public static JSONObject ok() {
    JSONObject ret = new JSONObject();
    ret.set("result", "success");
    ret.set("data", new JSONObject());
    return ret;
  }

  /**
   * 失败的时候提供失败信息
   *
   * @param message
   * @return
   */
  public static JSONObject fail(String message) {
    JSONObject ret = new JSONObject();
    ret.set("result", "fail");
    ret.set("message", message);
    return ret;
  }
}
