package com.soku.rebotcorner.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Rating {
  private Integer userId;
  private Integer gameId;
  private Integer score;
}
