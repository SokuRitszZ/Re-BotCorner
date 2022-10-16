package com.soku.rebotcorner.controller.account;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.account.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {
  @Autowired
  private LoginService loginService;

  @PostMapping("/api/account/token")
  public Map<String, String> getToken(@RequestBody JSONObject json) {
    String username = json.getString("username");
    String password = json.getString("password");
    return loginService.getToken(username, password);
  }
}
