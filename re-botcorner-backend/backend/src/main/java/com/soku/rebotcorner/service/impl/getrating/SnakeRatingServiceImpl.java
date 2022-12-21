package com.soku.rebotcorner.service.impl.getrating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.vo.RatingVoMapper;
import com.soku.rebotcorner.pojo.vo.RatingVo;
import com.soku.rebotcorner.service.getrating.SnakeRatingService;
import com.soku.rebotcorner.utils.NewRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SnakeRatingServiceImpl implements SnakeRatingService {
  @Autowired
  private RatingVoMapper mapper;

  @Override
  public JSONObject getRatingList() {
    List<RatingVo> snakeRatingList = mapper.getSnakeRatings();
    return NewRes.ok(new JSONObject().set("ratings", snakeRatingList));
  }
}
