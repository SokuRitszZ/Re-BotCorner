package com.soku.rebotcorner.service.group;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.utils.Res;
import org.springframework.web.multipart.MultipartFile;

public interface BGroupService {
  Res createGroup(MultipartFile file, JSONObject groupMessage);

  Res deleteGroup(Integer groupId);

  Res groupList();

  Res getById(Integer id);
}
