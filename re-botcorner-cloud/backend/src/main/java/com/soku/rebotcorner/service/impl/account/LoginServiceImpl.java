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
    UsernamePasswordAuthenticationToken authenticationToken =
      new UsernamePasswordAuthenticationToken(username, password); // 获取token
    Authentication authenticate = authenticationManager.authenticate(authenticationToken); // 验证token
    // 如果登录失败会自动处理
    UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal(); // 获取登录用户（这个用户是Security的）
    User user = loginUser.getUser();// 这个用户才是Pojo自己定义的
    String jwt = JwtUtil.createJWT(user.getId().toString()); // 通过工具类创建JWT
    Map<String, String> map = new HashMap<>();
    map.put("result", "success");
    map.put("token", jwt);
    return map;
  }
}
