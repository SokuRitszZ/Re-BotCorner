package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.RegisterService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisterServiceImpl implements RegisterService {
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public JSONObject register(String username, String password, String confirmedPassword) {
    try {
      if (username == null || (username = username.trim()).length() == 0) throw new Exception("用户名为空");
      if (username.length() > 32) throw new Exception("用户名长度超过32");
      if (password == null || confirmedPassword == null || password.length() == 0 || confirmedPassword.length() == 0)
        throw new Exception("密码为空");
      if (password.length() > 32 || confirmedPassword.length() > 32)
        throw new Exception("密码长度超过32");
      if (!password.equals(confirmedPassword))
        throw new Exception("两次密码不一致");

      QueryWrapper<User> qw = new QueryWrapper<>();
      qw.eq("username", username);
      List<User> users = UserDAO.mapper.selectList(qw);
      if (!users.isEmpty())
        throw new Exception("用户名已存在");

      String encodedPassword = passwordEncoder.encode(password);

      User user = new User()
        .setUsername(username)
        .setPassword(encodedPassword)
        .setAvatar("https://sdfsdf.dev/100x100.jpg");
      UserDAO.mapper.insert(user);

      return NewRes.ok(
        new JSONObject()
          .set("result", "success")
      );
    } catch (Exception e) {
      return NewRes.fail(e.getMessage());
    }
  }
}
