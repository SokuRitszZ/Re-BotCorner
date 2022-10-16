package com.soku.rebotcorner.service.impl.group;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soku.rebotcorner.mapper.BGroupMapper;
import com.soku.rebotcorner.pojo.BGroup;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.group.BGroupService;
import com.soku.rebotcorner.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class BGroupServiceImpl implements BGroupService {
  @Autowired
  private BGroupMapper bGroupMapper;
  /**
   * 创建小组
   *
   * @param file 图标
   * @param groupMessage 属性
   * @return
   */
  @Override
  public Res createGroup(MultipartFile file, JSONObject groupMessage) {
    String title = groupMessage.getStr("title").trim();
    String description = groupMessage.getStr("description").trim();
    if (title == null || title.length() == 0) return Res.fail("标题为空。");
    if (description == null || description.length() == 0) return Res.fail("描述为空。");
    Date createTime = new Date();
    UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
    Integer creatorId = loginUser.getUser().getId();

    String filename = title + "_" + RandomUtil.randomNumbers(10) + ".png";
    JSONObject saveResult = FileUtil.saveAndGetIcon(file, filename, "/group");
    String icon = saveResult.getStr("newUrl");

    BGroup group = new BGroup( null, title, icon, description, createTime, creatorId );
    bGroupMapper.insert(group);

    JSONObject json = JSONUtil.parseObj(group);
    json.put("creatorUsername", UserDAO.selectById(creatorId).getUsername());
    return Res.ok(group);
  }

  /**
   * 解散小组
   * @param groupId
   * @return
   */
  @Override
  public Res deleteGroup(Integer groupId) {
    BGroup group = bGroupMapper.selectById(groupId);
    if (group == null) return Res.fail("不存在此小组。");
    User user = JwtAuthenticationUtil.getCurrentUser();
    if (user.getId() != group.getCreatorId()) return Res.fail("没有权限解散小组。");
    bGroupMapper.deleteById(group);
    return Res.ok("okk");
  }

  /**
   * 获取所有小组
   *
   * @return
   */
  @Override
  public Res groupList() {
    List<BGroup> list = bGroupMapper.selectList(null);
    List<JSONObject> jsonList = new ArrayList<>();
    Map<Integer, String> usernameMap = new HashMap<>();
    for (BGroup gp: list) {
      JSONObject json = JSONUtil.parseObj(gp);
      Integer creatorId = json.getInt("creatorId");
      if (!usernameMap.containsKey(creatorId)) {
        User user = UserDAO.selectById(creatorId);
        usernameMap.put(creatorId, user.getUsername());
      }
      String username = usernameMap.get(creatorId);
      json.put("creatorUsername", username);
      jsonList.add(json);
    }
    return Res.ok(jsonList);
  }


  /**
   * 根据id获取小组
   *
   * @param id
   * @return
   */
  @Override
  public Res getById(Integer id) {
    BGroup group = bGroupMapper.selectById(id);
    JSONObject json = JSONUtil.parseObj(group);
    json.put("creatorUsername", UserDAO.selectById(group.getCreatorId()).getUsername());
    return Res.ok(json);
  }
}
