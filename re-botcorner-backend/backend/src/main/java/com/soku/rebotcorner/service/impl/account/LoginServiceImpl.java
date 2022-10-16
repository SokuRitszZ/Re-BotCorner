package com.soku.rebotcorner.service.impl.account;

import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.LoginService;
import com.soku.rebotcorner.utils.JwtUtil;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public Map<String, String> getToken(String username, String password) {
    Map<String, String> map = new HashMap<>();
    try {
      // 获取token（已经自动放在了请求头上）
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
      // 验证token
      Authentication authenticate = authenticationManager.authenticate(authenticationToken);
      // 如果登录失败会自动处理

      // 获取登录用户（这个用户是Security的）
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      // 这个用户才是Pojo定义的
      User user = loginUser.getUser();
      // 通过工具类创建JWT
      String jwt = JwtUtil.createJWT(user.getId().toString());
      map.put("result", "success");
      map.put("token", jwt);
      return map;
    } catch (Exception e) {
      // 登录失败的
      map.put("result", "fail");
      return map;
    }
  }
}
