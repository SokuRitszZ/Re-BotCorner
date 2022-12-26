package com.soku.rebotcorner.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotDAO {
  @Autowired
  public static BotMapper mapper;

  public static Bot selectById(Integer id) {
    return mapper.selectById(id);
  }

  public static Bot selectOne(QueryWrapper<Bot> qw) {
    return mapper.selectOne(qw);
  }
}
