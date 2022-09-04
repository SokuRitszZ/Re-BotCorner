package com.soku.rebotcorner.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {
  private Integer id;
  private String title;
  private String description;
  private String name;
}
