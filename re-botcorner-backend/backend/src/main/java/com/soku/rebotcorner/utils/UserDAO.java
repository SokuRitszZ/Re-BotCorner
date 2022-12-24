package com.soku.rebotcorner.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.mapper.vo.UserVoMapper;
import com.soku.rebotcorner.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAO {
  @Autowired
  public static UserMapper userMapper;
  public static UserVoMapper userVoMapper;

  public static void updateById(User user) {
    userMapper.updateById(user);
  };

  public static User selectById(Integer id) {
    return userMapper.selectById(id);
  }

  public static User selectOne(QueryWrapper<User> qw) {
    return userMapper.selectOne(qw);
  }

  public static List<User> selectList(QueryWrapper<User> qw) {
    return userMapper.selectList(qw);
  }

  public static void insert(User user) {
    userMapper.insert(user);
  }
}
