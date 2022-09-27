package com.soku.rebotcorner.controller.account;

import com.soku.rebotcorner.service.account.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {
  @Autowired
  private InfoService infoService;

  @GetMapping("/api/account/getInfo")
  public Map<String, String> getInfo() {
    return infoService.getInfo();
  }
}
