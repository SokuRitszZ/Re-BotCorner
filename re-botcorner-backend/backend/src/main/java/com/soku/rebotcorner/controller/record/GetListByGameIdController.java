package com.soku.rebotcorner.controller.record;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.bot.record.GetListByGameIdService;
import com.soku.rebotcorner.utils.Res;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.soku.rebotcorner.utils.ParseRecord.parseJSON;

@RestController
public class GetListByGameIdController {
  @Autowired
  private GetListByGameIdService getListByGameId;

  @GetMapping("/api/record/getListByGameId")
  public Res get(@RequestParam Map<String, String> params) {
    Integer gameId = Integer.parseInt(params.get("gameId"));

    List<Record> list = getListByGameId.getListByGameId(gameId);
    List<JSONObject> jsons = new ArrayList<>();
    for (Record record : list) jsons.add(parseJSON(record));
    return Res.ok(jsons);
  }
}
