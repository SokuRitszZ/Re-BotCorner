package com.soku.rebotcorner.service.impl.getrating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.SnakeRatingMapper;
import com.soku.rebotcorner.service.getrating.SnakeRatingService;
import com.soku.rebotcorner.utils.NewRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SnakeRatingServiceImpl implements SnakeRatingService {
  @Autowired
  private SnakeRatingMapper mapper;

  @Override
  public JSONObject getRatingList() {
    List<Map<String, Object>> top10 = mapper.getTop10();
    return NewRes.ok(new JSONObject().set("ratings", top10));
  }
}
