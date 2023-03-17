package com.soku.rebotcorner.controller.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.account.UpdateProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateProfileController {
  @Autowired
  private UpdateProfileService service;

  @PostMapping("/api/account/update")
  public JSONObject updateProfile(@RequestBody JSONObject json) {
    String username = json.getStr("username");
    String password = json.getStr("password");
    String signature = json.getStr("signature");
    return service.solve(username, password, signature);
  }
}
