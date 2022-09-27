package com.soku.rebotcorner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
