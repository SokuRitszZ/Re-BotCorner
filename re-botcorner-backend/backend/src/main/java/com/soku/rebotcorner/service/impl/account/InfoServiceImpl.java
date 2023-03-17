package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.InfoService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDAO;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class InfoServiceImpl implements InfoService {
  /**
   * 通过JWT获取用户信息
   *
   * @return {Map<String, String>} map
   */
  @Override
  public JSONObject getInfo() {
    try {
      UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
      User user = loginUser.getUser();
      return NewRes.ok(UserDAO.mapper.getProfileById(user.getId()));
    } catch (Exception e) {
      return NewRes.fail("Token无效");
    }
  }

  @Override
  public JSONObject getInfoById(Integer id) {
    try {
      return NewRes.ok(new JSONObject().set("result", UserDAO.mapper.getProfileById(id)));
    } catch (Exception e) {
      return NewRes.fail("获取失败");
    }
  }
}
