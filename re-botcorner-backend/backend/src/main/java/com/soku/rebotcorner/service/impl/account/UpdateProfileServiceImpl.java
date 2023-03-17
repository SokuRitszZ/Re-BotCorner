package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.UpdateProfileService;
import com.soku.rebotcorner.utils.JwtAuthenticationUtil;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UpdateProfileServiceImpl implements UpdateProfileService {
  @Autowired
  private PasswordEncoder encoder;

  @Override
  public JSONObject solve(String username, String password, String signature) {
    try {
      User user = JwtAuthenticationUtil.getCurrentUser();
      if (signature != null) {
        if (signature.length() > 32)
          throw new Exception("个性签名长度大于32");
        user.setSignature(signature);
      }
      if (username != null) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username);
        if (!UserDAO.mapper.selectList(qw).isEmpty()) {
          throw new Exception("用户名已存在");
        }
        if (username.length() > 32) throw new Exception("名字长度大于32");
        user.setUsername(username);
      }
      if (password != null) {
        if (password.length() > 32) throw new Exception("密码长度超过32");
        String encodedPassword = encoder.encode(password);
        user.setPassword(encodedPassword);
      }
      UserDAO.mapper.updateById(user);
      return NewRes.ok();
    } catch (Exception e) {
      return NewRes.fail(e.getMessage());
    }
  }
}
