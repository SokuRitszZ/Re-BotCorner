package com.soku.rebotcorner.controller.record;

import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.bot.record.GetListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GetListController {
  @Autowired
  private GetListService getListService;

  @GetMapping("/api/record/getAll")
  List<Record> getList() {
    return getListService.getList();
  }
}
