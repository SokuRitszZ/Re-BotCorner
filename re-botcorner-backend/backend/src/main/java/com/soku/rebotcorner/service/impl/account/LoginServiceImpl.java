package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.LoginService;
import com.soku.rebotcorner.utils.JwtUtil;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public JSONObject getToken(String username, String password) {
    try {
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
      Authentication authenticate = authenticationManager.authenticate(authenticationToken);

      // 获取登录用户（这个用户是Security的）
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      // 这个用户才是Pojo定义的
      User user = loginUser.getUser();
      // 通过工具类创建JWT
      String jwt = JwtUtil.createJWT(user.getId().toString());
      return NewRes.ok(new JSONObject().set("token", jwt));
    } catch (Exception e) {
      // 登录失败的
      return NewRes.fail("密码错误");
    }
  }
}
