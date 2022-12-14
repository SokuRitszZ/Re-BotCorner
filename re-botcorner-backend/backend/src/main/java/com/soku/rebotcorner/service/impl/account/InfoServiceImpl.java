package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.InfoService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {
  /**
   * 通过JWT获取用户信息
   *
   * @return {Map<String, String>} map
   */
  @Override
  public JSONObject getInfo() {
    Map<String, String> map = new HashMap<>();
    try {
      UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
      User user = loginUser.getUser();
      JSONObject json = new JSONObject();
      json.set("id", user.getId());
      json.set("username", user.getUsername());
      json.set("headIcon", user.getHeadIcon());
      return NewRes.ok(json);
    } catch (Exception e) {
      return NewRes.fail("Token无效");
    }
  }
}
