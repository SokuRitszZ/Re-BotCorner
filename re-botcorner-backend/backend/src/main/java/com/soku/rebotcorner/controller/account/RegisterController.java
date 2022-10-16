package com.soku.rebotcorner.controller.account;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RegisterController {
  @Autowired
  private RegisterService registerService;

  @PostMapping("/api/account/register/")
  public Map<String, String> register(@RequestBody JSONObject json) {
    String username = json.getString("username");
    String password = json.getString("password");
    String confirmedPassword = json.getString("confirmedPassword");
    return registerService.register(username, password, confirmedPassword);
  }
}
