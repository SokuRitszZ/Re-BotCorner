package com.soku.rebotcorner.controller.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.account.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
  @Autowired
  private LoginService loginService;

  @PostMapping("/api/account/token")
  public JSONObject getToken(@RequestBody JSONObject json) {
    String username = json.getStr("username");
    String password = json.getStr("password");
    return loginService.getToken(username, password);
  }
}
