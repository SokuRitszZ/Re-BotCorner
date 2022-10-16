package com.soku.rebotcorner.controller.account;

import com.alibaba.fastjson.JSONObject;
import com.soku.rebotcorner.service.account.PhoneAuthorizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PhoneAuthorizeController {
  @Autowired
  private PhoneAuthorizeService phoneAuthorizeService;

  @PostMapping("/api/account/phoneauth")
  public Map<String, String> phoneLogin(@RequestBody JSONObject json) {
    String phone = json.getString("phone");
    return phoneAuthorizeService.phoneAuthorize(phone);
  }
}
