package com.soku.rebotcorner.controller.lang;

import com.soku.rebotcorner.mapper.LangMapper;
import com.soku.rebotcorner.pojo.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LangController {
  @Autowired
  private LangMapper langMapper;

  @GetMapping("/api/lang/getAll")
  public List<Lang> getAll() {
    return langMapper.selectList(null);
  }
}
