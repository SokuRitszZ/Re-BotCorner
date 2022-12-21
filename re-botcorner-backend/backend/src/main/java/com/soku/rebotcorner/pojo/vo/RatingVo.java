package com.soku.rebotcorner.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingVo {
  String headIcon;
  String username;
  Integer rating;
  Integer id;
}