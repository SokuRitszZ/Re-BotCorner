package com.soku.rebotcorner.service.impl.record;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.RecordMapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.Record;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.record.GetListByGameId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetListByGameIdImpl implements GetListByGameId {
  @Autowired
  private RecordMapper recordMapper;
  @Autowired
  private UserMapper userMapper;

  @Override
  public List<Map<String, String>> get(Map<String, String> data) {
    Integer gameId = Integer.parseInt(data.get("gameId"));
    QueryWrapper<Record> qw = new QueryWrapper<>();
    qw.eq("game_id", gameId);
    List<Record> recordList = recordMapper.selectList(qw);
    List<Map<String, String>> infos = new ArrayList<>();
    for (Record record: recordList) {
      Map<String, String> map = new HashMap<>();
      map.put("id", record.getId().toString());
      map.put("json", record.getJson());
      String timeFMT = "yyyy-MM-dd HH:mm:ss";
      SimpleDateFormat sdf = new SimpleDateFormat(timeFMT);
      map.put("createTime", sdf.format(record.getCreateTime()));

      Integer userId0 = record.getUserId0();
      Integer userId1 = record.getUserId1();
      User user0 = userMapper.selectById(userId0);
      User user1 = userMapper.selectById(userId1);
      String username0 = user0.getUsername();
      String username1 = user1.getUsername();
      String headIcon0 = user0.getHeadIcon();
      String headIcon1 = user1.getHeadIcon();
      Integer result = record.getResult();
      map.put("userId0", userId0.toString());
      map.put("userId1", userId1.toString());
      map.put("username0", username0);
      map.put("username1", username1);
      map.put("headIcon0", headIcon0);
      map.put("headIcon1", headIcon1);
      map.put("result", result.toString());

      infos.add(map);
    }
    return infos;
  }
}
