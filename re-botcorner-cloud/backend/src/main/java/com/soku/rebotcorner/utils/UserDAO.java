package com.soku.rebotcorner.utils;

import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDAO {
  @Autowired
  public static UserMapper userMapper;

  public static void updateById(User user) {
    userMapper.updateById(user);
  };
}
