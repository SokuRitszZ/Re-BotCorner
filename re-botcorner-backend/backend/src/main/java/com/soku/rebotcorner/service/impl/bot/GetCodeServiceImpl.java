package com.soku.rebotcorner.service.impl.bot;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.service.bot.GetCodeService;
import com.soku.rebotcorner.utils.NewRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetCodeServiceImpl implements GetCodeService {
  @Autowired
  private BotMapper mapper;

  @Override
  public JSONObject getCode(int id) {
    try {
      Bot bot = mapper.selectById(id);
      return NewRes.ok(new JSONObject().set("code", bot.getCode()));
    } catch (Exception e) {
      return NewRes.fail("不存在此Bot");
    }
  }
}
