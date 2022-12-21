package com.soku.rebotcorner.service.impl.bot;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.bot.DeleteService;
import com.soku.rebotcorner.utils.NewRes;
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
  public JSONObject deleteBot(Integer id) {
    try {
      UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      User user = loginUser.getUser();

      QueryWrapper<Bot> qw = new QueryWrapper<>();
      Bot bot = botMapper.selectById(id);
      if (bot == null) { return NewRes.fail("不存在此Bot"); }
      if (bot.getUserId() != user.getId()) { return NewRes.fail("没有权限删除此Bot"); }
      botMapper.deleteById(id);

      return NewRes.ok();
    } catch (Exception e) {
      return NewRes.fail("Token无效");
    }
  }
}
