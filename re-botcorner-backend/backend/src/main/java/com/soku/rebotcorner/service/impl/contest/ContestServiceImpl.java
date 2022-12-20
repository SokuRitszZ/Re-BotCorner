package com.soku.rebotcorner.service.impl.contest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BGroupMapper;
import com.soku.rebotcorner.mapper.BGroupMemberMapper;
import com.soku.rebotcorner.mapper.ContestMapper;
import com.soku.rebotcorner.pojo.BGroup;
import com.soku.rebotcorner.pojo.Contest;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.contest.ContestService;
import com.soku.rebotcorner.utils.JwtAuthenticationUtil;
import com.soku.rebotcorner.utils.Res;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ContestServiceImpl implements ContestService {
  @Autowired
  private BGroupMapper bGroupMapper;

  @Autowired
  private ContestMapper mapper;

  /**
   * 创建比赛
   * @param title
   * @param groupId
   * @param gameId
   * @param rule
   * @param time
   * @return
   */
  @Override
  public Res createContest(String title, Integer groupId, Integer gameId, Integer rule, Date time) {
    User user = JwtAuthenticationUtil.getCurrentUser();
    BGroup group = bGroupMapper.selectById(groupId);

    if (group == null) return Res.fail("此小组不存在");

    if (group.getCreatorId() != user.getId()) return Res.fail("没有权限添加比赛");

    if (title == null || title.trim().length() == 0) title = String.format("比赛_%d_%d", groupId, gameId);

    if (title.length() > 32) return Res.fail("标题超过32字");

    Contest contest = new Contest(
      null,
      groupId,
      gameId,
      rule,
      0,
      time,
      title
    );
    mapper.insert(contest);
    return Res.ok(contest);
  }

  /**
   * 获取比赛
   * @param groupId
   * @return
   */
  @Override
  public Res getContests(Integer groupId) {
    List<Contest> contests = mapper.selectList(new QueryWrapper<Contest>().eq("group_id", groupId));
    return Res.ok(contests);
  }

  /**
   * 删除比赛
   * @param contestId
   */
  @Override
  public void removeContest(Integer contestId) {
    mapper.deleteById(contestId);
  }
}
