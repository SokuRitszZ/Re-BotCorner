package com.soku.rebotcorner.controller.bot;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.bot.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
public class BotController {
  @Autowired
  private BotService service;

  @PostMapping("/add")
  public JSONObject addBot(@RequestBody JSONObject json) {
    return service.addBot(json);
  }

  @DeleteMapping("/delete")
  public JSONObject deleteBot(@RequestParam("id") Integer id) {
    return service.deleteBot(id);
  }

  @GetMapping("/getall")
  public JSONObject getAll() {
    return service.getAll();
  }

  @GetMapping("/getcode")
  public JSONObject getCode(@RequestParam("id") Integer id) {
    return service.getCode(id);
  }

  @PutMapping("/update")
  public JSONObject updateBot(@RequestBody JSONObject json) {
    return service.updateBot(json);
  }

  @PutMapping("/visible")
  public JSONObject changeVisible(@RequestBody JSONObject json) {
    Integer id = json.getInt("id");
    Boolean visible = json.getBool("visible");
    return service.changeVisible(id, visible);
  }
}
