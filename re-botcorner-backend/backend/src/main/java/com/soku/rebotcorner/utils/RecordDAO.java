package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.RecordMapper;
import com.soku.rebotcorner.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordDAO {
  @Autowired
  public static RecordMapper mapper;
}
