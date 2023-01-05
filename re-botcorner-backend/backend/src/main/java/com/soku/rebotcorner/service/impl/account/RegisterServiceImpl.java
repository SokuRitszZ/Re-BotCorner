package com.soku.rebotcorner.service.impl.account;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.RegisterService;
import com.soku.rebotcorner.utils.NewRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisterServiceImpl implements RegisterService {
  @Autowired
  private UserMapper mapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public JSONObject register(String username, String password, String confirmedPassword) {
    // 检测格式
    if (username == null) {
      return NewRes.fail("用户名为空");
    } else if (password == null || confirmedPassword == null || password.length() == 0 || confirmedPassword.length() == 0) {
      return NewRes.fail("密码为空");
    }
    username = username.trim();
    if (username.length() == 0) {
      return NewRes.fail("用户名为空");
    } else if (username.length() > 32) {
      return NewRes.fail("用户名长度超过32");
    } else if (password.length() > 32 || confirmedPassword.length() > 32) {
      return NewRes.fail("密码长度超过32");
    } else if (!password.equals(confirmedPassword)) {
      return NewRes.fail("两次密码不一致");
    }

    QueryWrapper<User> qw = new QueryWrapper<>();
    qw.eq("username", username);
    List<User> users = mapper.selectList(qw);
    if (!users.isEmpty()) {
      return NewRes.fail("用户名已存在");
    }

    // 加密密码
    String encodedPassword = passwordEncoder.encode(password);
    // 初始化用户信息
    User user = new User(
      null,
      username,
      encodedPassword,
      "https://sdfsdf.dev/500x500.jpg,0000ff,ffff00",
      1500,
      null,
      null
    );
    mapper.insert(user);
    return NewRes.ok(
      new JSONObject()
        .set("result", "success")
    );
  }
}
