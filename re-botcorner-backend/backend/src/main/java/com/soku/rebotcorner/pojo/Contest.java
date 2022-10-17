package com.soku.rebotcorner.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contest {
  // 主键
  @TableId(type = IdType.AUTO)
  private Integer id;
  // 小组
  private Integer groupId;
  // 游戏
  private Integer gameId;
  // 赛制
  private Integer rule;
  // 状态
  private Integer state;
  // 开始时间
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date startTime;
  // 标题
  private String title;
}
