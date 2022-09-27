package com.soku.rebotcorner.service.impl.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.bot.DeleteService;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzTransactionManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DeleteServiceImpl implements DeleteService {
  @Autowired
  private BotMapper botMapper;

  @Override
  public Map<String, String> deleteBot(Map<String, String> data) {
    UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
    User user = loginUser.getUser();

    Map<String, String> map = new HashMap<>();
    Integer id = Integer.parseInt(data.get("id"));
    QueryWrapper<Bot> qw = new QueryWrapper<>();
    Bot bot = botMapper.selectById(id);
    if (bot == null) {
      map.put("result", "不存在此Bot");
      return map;
    } else if (bot.getUserId() != user.getId()) {
      map.put("result", "没有权限删除此Bot");
      return map;
    }
    botMapper.deleteById(id);
    map.put("result", "success");
    return map;
  }
}
