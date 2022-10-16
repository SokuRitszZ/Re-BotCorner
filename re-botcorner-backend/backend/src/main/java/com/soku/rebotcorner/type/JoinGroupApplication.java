package com.soku.rebotcorner.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinGroupApplication {
  private Integer applicantId;
  private Integer groupId;
  private Integer creatorId;
  private String application;
}
