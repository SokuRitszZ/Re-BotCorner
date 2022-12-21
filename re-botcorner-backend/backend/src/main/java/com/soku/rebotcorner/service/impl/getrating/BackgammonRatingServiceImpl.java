package com.soku.rebotcorner.service.impl.getrating;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.BackgammonRatingMapper;
import com.soku.rebotcorner.mapper.vo.RatingVoMapper;
import com.soku.rebotcorner.pojo.BackgammonRating;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.pojo.vo.RatingVo;
import com.soku.rebotcorner.service.getrating.BackgammonRatingService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackgammonRatingServiceImpl implements BackgammonRatingService {
  @Autowired
  private RatingVoMapper mapper;

  @Override
  public JSONObject getRatingList() {
    List<RatingVo> backgammonRatings = mapper.getBackgammonRating();
    return NewRes.ok(new JSONObject().set("ratings", backgammonRatings));
  }
}
