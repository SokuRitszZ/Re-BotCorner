package com.soku.rebotcorner.service.impl.bot;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.bot.GetAllService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllServiceImpl implements GetAllService {
  @Autowired
  private BotMapper botMapper;

  @Override
  public JSONObject getAll() {
    try {
      UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      User user = loginUser.getUser();

      QueryWrapper<Bot> qw = new QueryWrapper<>();
      qw.eq("user_id", user.getId());
      List<Bot> bots = botMapper.selectList(qw);
      for (Bot bot : bots) { bot.setCode(""); }
      return NewRes.ok(new JSONObject().set("bots", bots));
    } catch (Exception e) {
      return NewRes.fail("Token 无效");
    }
  }
}
