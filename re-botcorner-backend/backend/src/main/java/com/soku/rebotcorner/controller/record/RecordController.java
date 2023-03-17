package com.soku.rebotcorner.controller.record;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.consumer.GameSocketServer;
import com.soku.rebotcorner.service.record.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/record")
@RestController
public class RecordController {
  @Autowired
  private RecordService service;

  @GetMapping("/get")
  public JSONObject getBaseByGameId(@RequestParam("gameId") Integer gameId, @RequestParam("from") Integer from, @RequestParam("count") Integer count) {
    return service.getBaseRecordByGameId(gameId, from, count);
  }

  @GetMapping("/count")
  public JSONObject getRecordCount(@RequestParam("gameId") Integer gameId) {
    return service.getRecordCount(gameId);
  }

  @GetMapping("/json")
  public JSONObject getRecordJson(@RequestParam("id") Integer id) {
    return service.getRecordJson(id);
  }

  @GetMapping("/top")
  public JSONObject getTopRecord() {
    return service.getTopRecord();
  }

  @GetMapping("/current")
  public JSONObject getCurrent() {
    return GameSocketServer.getCurrentPlayingMatches();
  }
}