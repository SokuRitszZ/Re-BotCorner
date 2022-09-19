package com.soku.rebotcorner.controller.user;

import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
  @Autowired
  UserMapper userMapper;

  @GetMapping("/api/user/all")
  public List<User> getAll() {
    return userMapper.selectList(null);
  }

  @GetMapping("/api/user/{userId}")
  public User getUser(@PathVariable int userId) {
    return userMapper.selectById(userId);
  }
}
