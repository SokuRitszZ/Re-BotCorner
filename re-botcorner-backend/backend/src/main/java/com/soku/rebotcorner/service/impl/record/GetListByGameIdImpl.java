package com.soku.rebotcorner.service.impl.record;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.RecordMapper;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.record.GetListByGameIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GetListByGameIdImpl implements GetListByGameIdService {
  @Autowired
  private RecordMapper recordMapper;

  @Override
  public List<Record> getListByGameId(Map<String, String> data) {
    Integer gameId = Integer.parseInt(data.get("gameId"));
    QueryWrapper<Record> qw = new QueryWrapper<>();
    qw.eq("game_id", gameId);
    return recordMapper.selectList(qw);
  }
}
