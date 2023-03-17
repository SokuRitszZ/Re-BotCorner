package com.soku.rebotcorner.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class User {
  @TableId(type = IdType.AUTO)
  private Integer id;
  private String username;
  private String password;
  private String avatar;
  private String openid;
  private String phone;
  private String signature;
}
