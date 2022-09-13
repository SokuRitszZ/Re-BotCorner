package com.soku.rebotcorner.service.impl.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.bot.GetByGameService;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GetByGameServiceImpl implements GetByGameService {
  @Autowired
  private BotMapper botMapper;

  @Override
  public List<Bot> getByGame(Map<String, String> data) {
    UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
    User user = loginUser.getUser();

    Integer gameId = Integer.parseInt(data.get("gameId"));
    QueryWrapper<Bot> qw = new QueryWrapper<>();
    qw.eq("game_id", gameId).eq("user_id", user.getId());

    return botMapper.selectList(qw);
  }
}
