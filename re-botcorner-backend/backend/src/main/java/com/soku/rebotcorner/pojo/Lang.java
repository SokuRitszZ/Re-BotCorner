package com.soku.rebotcorner.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lang {
  private Integer id;
  private String lang;
  private String suffix;
}
