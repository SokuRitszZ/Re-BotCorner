package com.soku.rebotcorner.service.impl.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.bot.GetAllService;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllServiceImpl implements GetAllService {
  @Autowired
  private BotMapper botMapper;

  @Override
  public List<Bot> getAll() {
    UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
    User user = loginUser.getUser();

    QueryWrapper<Bot> qw = new QueryWrapper<>();
    qw.eq("user_id", user.getId());

    return botMapper.selectList(qw);
  }
}
