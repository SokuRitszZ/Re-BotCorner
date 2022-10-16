package com.soku.rebotcorner.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BGroupMember {
  private Integer groupId;
  private Integer userId;
}
