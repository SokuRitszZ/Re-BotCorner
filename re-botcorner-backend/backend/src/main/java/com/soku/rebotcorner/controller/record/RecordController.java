package com.soku.rebotcorner.controller.record;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.bot.record.GetListByGameIdService;
import com.soku.rebotcorner.service.bot.record.RecordService;
import com.soku.rebotcorner.utils.Res;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/record")
@RestController
public class RecordController {
  @Autowired
  private RecordService service;

  @GetMapping("/get")
  public JSONObject get(@RequestParam("gameId") Integer gameId, @RequestParam("from") Integer from, @RequestParam("count") Integer count) {
    return service.getBaseRecordByGameId(gameId, from, count);
  }
}
