package com.soku.rebotcorner.service.impl.record;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.RecordMapper;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.service.record.GetListByGameIdService;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetListByGameIdImpl implements GetListByGameIdService {
  @Autowired
  private RecordMapper recordMapper;

  @Override
  public List<Record> getListByGameId(Map<String, String> data) {
    Integer gameId = Integer.parseInt(data.get("gameId"));
    // 这里不能直接存下headicon，要从数据库中查找过来然后放入数据中
    QueryWrapper<Record> qw = new QueryWrapper<>();
    qw.eq("game_id", gameId);
    List<Record> recordList = recordMapper.selectList(qw);
    Map<Integer, String> hasFound = new HashMap<>();
    for (Record record: recordList) {
      int userId0 = record.getUserId0();
      int userId1 = record.getUserId1();
      if (!hasFound.containsKey(userId0)) hasFound.put(userId0, UserDAO.selectById(userId0).getHeadIcon());
      if (!hasFound.containsKey(userId1)) hasFound.put(userId1, UserDAO.selectById(userId1).getHeadIcon());
      record.setHeadIcon0(hasFound.get(userId0));
      record.setHeadIcon1(hasFound.get(userId1));
    }
    return recordList;
  }
}
