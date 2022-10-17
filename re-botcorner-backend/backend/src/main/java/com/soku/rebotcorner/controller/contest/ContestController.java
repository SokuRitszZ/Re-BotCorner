package com.soku.rebotcorner.controller.contest;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.service.contest.ContestService;
import com.soku.rebotcorner.utils.Res;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RequestMapping("/api/contest")
@RestController
public class ContestController {
  @Autowired
  private ContestService service;

  @PostMapping("/create")
  Res createContest(@RequestBody JSONObject json) {
    String title = json.getStr("title");
    Integer groupId = json.getInt("groupId");
    Integer gameId = json.getInt("gameId");
    Integer rule = json.getInt("rule");
    Date time = null;
    try {
      time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getStr("time"));
    } catch (ParseException e) {
      return Res.fail("时间转换出错");
    }
    return service.createContest(title, groupId, gameId, rule, time);
  }

  @GetMapping("/getAll")
  Res getContests(@RequestParam Map<String, String> data) {
    Integer groupId = Integer.parseInt(data.get("groupId"));
    return service.getContests(groupId);
  }
}
