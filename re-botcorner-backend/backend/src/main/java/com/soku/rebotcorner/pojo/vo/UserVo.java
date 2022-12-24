package com.soku.rebotcorner.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {
  private Integer id;
  private String username;
  private String headIcon;
}
