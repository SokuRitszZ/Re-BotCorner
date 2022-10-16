package com.soku.rebotcorner.controller.account;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.account.PhoneLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PhoneLoginController {
  @Autowired
  private PhoneLoginService phoneLoginService;

  @PostMapping("/api/account/phonelogin")
  public Map<String, String> phoneLogin(@RequestBody JSONObject json) {
    String phone = json.getString("phone");
    String authCode = json.getString("authCode");
    return phoneLoginService.phoneLogin(phone, authCode);
  }
}
