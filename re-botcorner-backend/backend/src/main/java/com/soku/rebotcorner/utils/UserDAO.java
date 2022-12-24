package com.soku.rebotcorner.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAO {
  @Autowired
  public static UserMapper mapper;
}
