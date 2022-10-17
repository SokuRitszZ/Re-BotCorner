package com.soku.rebotcorner.service.contest;

import com.soku.rebotcorner.utils.Res;

import java.util.Date;

public interface ContestService {
  Res createContest(String title, Integer groupId, Integer gameId, Integer rule, Date time);

  Res getContests(Integer groupId);
}
