package com.soku.rebotcorner.service.impl.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {
  @Autowired
  private UserMapper userMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public Map<String, String> register(String username, String password, String confirmedPassword) {
    Map<String, String> map = new HashMap<>();
    // 检测格式
    if (username == null) {
      map.put("result", "用户名为空");
      return map;
    } else if (password == null || confirmedPassword == null || password.length() == 0 || confirmedPassword.length() == 0) {
      map.put("result", "密码为空");
      return map;
    }
    username = username.trim();
    if (username.length() == 0) {
      map.put("result", "用户名为空");
      return map;
    } else if (username.length() > 32) {
      map.put("result", "用户名长度大于32");
      return map;
    } else if (password.length() > 32 || confirmedPassword.length() > 32) {
      map.put("result", "密码长度大于32");
      return map;
    } else if (!password.equals(confirmedPassword)) {
      map.put("result", "两次密码不一致");
      return map;
    }

    QueryWrapper<User> qw = new QueryWrapper<>();
    qw.eq("username", username);
    List<User> users = userMapper.selectList(qw);
    if (!users.isEmpty()) {
      map.put("result", "用户名已存在");
      return map;
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
    userMapper.insert(user);
    map.put("result", "success");
    return map;
  }
}
