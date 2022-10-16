package com.soku.rebotcorner.service.impl.record;

import com.soku.rebotcorner.mapper.RecordMapper;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.bot.record.GetListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetListServiceImpl implements GetListService {

  @Autowired
  private RecordMapper recordMapper;

  @Override
  public List<Record> getList() {
    return recordMapper.selectList(null);
  }
}
