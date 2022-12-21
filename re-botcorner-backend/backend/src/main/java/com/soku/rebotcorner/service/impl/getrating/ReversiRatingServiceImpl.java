package com.soku.rebotcorner.service.impl.getrating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.ReversiRatingMapper;
import com.soku.rebotcorner.mapper.vo.RatingVoMapper;
import com.soku.rebotcorner.pojo.ReversiRating;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.pojo.vo.RatingVo;
import com.soku.rebotcorner.service.getrating.ReversiRatingService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReversiRatingServiceImpl implements ReversiRatingService {
  @Autowired
  private RatingVoMapper mapper;

  @Override
  public JSONObject getRatingList() {
    List<RatingVo> reversiRatings = mapper.getReversiRating();
    return NewRes.ok(new JSONObject().set("ratings", reversiRatings));
  }
}
