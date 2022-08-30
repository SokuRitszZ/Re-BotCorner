package com.soku.rebotcorner.service.impl.account;

import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.InfoService;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {
  @Override
  public Map<String, String> getInfo() {
    // 通过token获取上下文
    UsernamePasswordAuthenticationToken authentication
      = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

    UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
    User user = loginUser.getUser();

    Map<String, String> map = new HashMap<>();
    map.put("result", "success");
    map.put("id", user.getId().toString());
    map.put("username", user.getUsername());
    map.put("headIcon", user.getHeadIcon());
    return map;
  }
}
