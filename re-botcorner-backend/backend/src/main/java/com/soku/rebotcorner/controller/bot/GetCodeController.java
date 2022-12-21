package com.soku.rebotcorner.controller.bot;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.bot.GetCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetCodeController {
  @Autowired
  private GetCodeService service;

  @GetMapping("/api/bot/getcode")
  public JSONObject getCode(@RequestParam("id") Integer id) {
    return service.getCode(id);
  }
}
