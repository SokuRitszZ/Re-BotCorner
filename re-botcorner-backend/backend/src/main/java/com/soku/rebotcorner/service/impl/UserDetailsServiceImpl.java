package com.soku.rebotcorner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** 用于通过用户名获取用户信息 */
/** 让数据库的信息可以对接Security，也能通过User表来登录Security */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired
  private UserMapper userMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    QueryWrapper<User> qw = new QueryWrapper<>();
    qw.eq("username", username);
    User user = userMapper.selectOne(qw);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    return new UserDetailsImpl(user);
  }
}
