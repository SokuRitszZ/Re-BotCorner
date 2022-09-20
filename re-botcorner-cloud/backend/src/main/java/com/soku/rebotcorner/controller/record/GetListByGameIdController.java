package com.soku.rebotcorner.controller.record;

import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.record.GetListByGameIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GetListByGameIdController {
  @Autowired
  private GetListByGameIdService getListByGameId;

  @GetMapping("/api/record/getListByGameId")
  public List<Record> get(@RequestParam Map<String, String> data) {
    return getListByGameId.getListByGameId(data);
  }
}
