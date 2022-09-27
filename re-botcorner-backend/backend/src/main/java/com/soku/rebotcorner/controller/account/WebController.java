package com.soku.rebotcorner.controller.account;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.account.acwing.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WebController {
  @Autowired
  private WebService service;

  @GetMapping("/api/user/account/acwing/web/apply_code/")
  public JSONObject applyCode() {
    return service.applyCode();
  }

  @GetMapping("/api/user/account/acwing/web/receive_code/")
  public JSONObject receiveCode(@RequestParam Map<String, String> data) {
    String code = data.get("code");
    String state = data.get("state");
    return service.receiveCode(code, state);
  }
}
