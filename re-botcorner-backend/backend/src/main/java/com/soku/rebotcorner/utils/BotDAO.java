package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.BotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotDAO {
  @Autowired
  public static BotMapper mapper;
}
