package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.LangMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LangDAO {
  @Autowired
  public static LangMapper langMapper;

  public static String getSuffix(Integer id) {
    return langMapper.selectById(id).getSuffix();
  }

  public static String getLang(Integer id) {
    return langMapper.selectById(id).getLang();
  }
}
