package com.soku.rebotcorner.controller.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {
  @Autowired
  private RegisterService registerService;

  @PostMapping("/api/account/register/")
  public JSONObject register(@RequestBody JSONObject json) {
    String username = json.getStr("username");
    String password = json.getStr("password");
    String confirmedPassword = json.getStr("confirmed_password");
    return registerService.register(username, password, confirmedPassword);
  }
}
