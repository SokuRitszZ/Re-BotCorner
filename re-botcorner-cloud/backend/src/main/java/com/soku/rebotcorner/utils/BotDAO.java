package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotDAO {
  @Autowired
  public static BotMapper botMapper;

  public static Bot getById(Integer id) {
    return botMapper.selectById(id);
  }
}
