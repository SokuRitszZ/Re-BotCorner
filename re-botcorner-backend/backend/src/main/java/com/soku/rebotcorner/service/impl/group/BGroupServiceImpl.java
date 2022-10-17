package com.soku.rebotcorner.service.impl.group;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BGroupMapper;
import com.soku.rebotcorner.mapper.BGroupMemberMapper;
import com.soku.rebotcorner.pojo.BGroup;
import com.soku.rebotcorner.pojo.BGroupMember;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.group.BGroupService;
import com.soku.rebotcorner.type.JoinGroupApplication;
import com.soku.rebotcorner.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.soku.rebotcorner.utils.RedisConstants.CACHE_APPLICATION_KEY;

@Service
public class BGroupServiceImpl implements BGroupService {
  @Autowired
  private BGroupMapper bGroupMapper;

  @Autowired
  private BGroupMemberMapper bGroupMemberMapper;

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

    // 把自己加进小组里面
    bGroupMemberMapper.insert(new BGroupMember( group.getId(), creatorId ));

    JSONObject json = JSONUtil.parseObj(group);
    json.put("creatorUsername", UserDAO.selectById(creatorId).getUsername());
    return Res.ok(json);
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
    // 清空成员（必须先清空成员，不然外键约束导致无法删除小组
    bGroupMemberMapper.delete(new QueryWrapper<BGroupMember>().eq("group_id", groupId));

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
   * 根据id获取小组顺便检查自己是否在这个小组内
   *
   * @param id
   * @return
   */
  @Override
  public Res getById(Integer id) {
    BGroup group = bGroupMapper.selectById(id);
    if (group == null) return Res.fail("不存在此小组");
    User user = JwtAuthenticationUtil.getCurrentUser();
    JSONObject json = JSONUtil.parseObj(group);
    json.put("creatorUsername", UserDAO.selectById(group.getCreatorId()).getUsername());
    json.put("isIn", bGroupMemberMapper.selectOne(new QueryWrapper<BGroupMember>().eq("group_id", id).eq("user_id", user.getId())) != null);
    return Res.ok(json);
  }

  /**
   * 提交加入申请
   *
   * @param groupId
   * @param application
   * @return
   */
  @Override
  public Res apply(Integer groupId, String application) {
    User user = JwtAuthenticationUtil.getCurrentUser();
    BGroup group = bGroupMapper.selectById(groupId);
    Integer creatorId = group.getCreatorId();
    String applicationKey = CACHE_APPLICATION_KEY + creatorId + ":" + groupId + ":" + user.getId();

    if (CacheClient.containsKey(applicationKey)) return Res.fail("上一个申请暂未处理或过期");

    JoinGroupApplication joinGroupApplication = new JoinGroupApplication(
      user.getId(),
      groupId,
      creatorId,
      application
    );

    CacheClient.set(applicationKey, joinGroupApplication, 1L, TimeUnit.DAYS);
    return Res.ok("成功提交申请，请等候消息");
  }

  /**
   * 获取所有由自己处理的申请
   *
   * @return
   */
  @Override
  public Res getApplication() {
    Integer userId = JwtAuthenticationUtil.getCurrentUser().getId();
    String key = CACHE_APPLICATION_KEY + userId + ":*";
    List<String> datas = CacheClient.getByPattern(key);
    List<JSONObject> applications = new ArrayList<>();
    Map<Integer, User> userMap = new HashMap<>();
    Map<Integer, BGroup> groupMap = new HashMap<>();
    datas.forEach(item -> {
      JSONObject json = JSONUtil.parseObj(item);
      Integer applicantId = json.getInt("applicantId");
      Integer groupId = json.getInt("groupId");
      if (!userMap.containsKey(applicantId)) userMap.put(applicantId, UserDAO.selectById(applicantId));
      if (!groupMap.containsKey(groupId)) groupMap.put(groupId, bGroupMapper.selectById(groupId));
      User user = userMap.get(applicantId);
      BGroup group = groupMap.get(groupId);
      json.put("applicantUsername", user.getUsername());
      json.put("applicantHeadIcon", user.getHeadIcon());
      if (group != null) {
        json.put("groupIcon", group.getIcon());
        json.put("groupTitle", group.getTitle());
      }
      applications.add(json);
    });
    return Res.ok(applications);
  }

  /**
   * 处理申请
   *
   * @param groupId
   * @param applicantId
   * @param state
   * @return
   */
  @Override
  public Res handleApplication(Integer groupId, Integer applicantId, Boolean state) {
    User user = JwtAuthenticationUtil.getCurrentUser();
    String key = CACHE_APPLICATION_KEY + user.getId() + ":" + groupId + ":" + applicantId;
    if (!CacheClient.deleteKey(key)) return Res.fail("不存在此申请");
    if (state) bGroupMemberMapper.insert(new BGroupMember( groupId, applicantId ));
    return Res.ok("处理完毕");
  }

  /**
   * 获取某个组的所有组员
   *
   * @param groupId
   * @return
   */
  @Override
  public Res getMembers(Integer groupId) {
    List<BGroupMember> members = bGroupMemberMapper.selectList(new QueryWrapper<BGroupMember>().eq("group_id", groupId));
    List<JSONObject> infos = new ArrayList<>();
    members.forEach(member -> {
      JSONObject json = JSONUtil.parseObj(member);
      User user = UserDAO.selectById(json.getInt("userId"));
      json.put("id", user.getId());
      json.put("headIcon", user.getHeadIcon());
      json.put("username", user.getUsername());
      infos.add(json);
    });
    return Res.ok(infos);
  }

  /**
   * 退出小组
   *
   * @param groupId
   * @return
   */
  @Override
  public Res resign(Integer groupId) {
    User user = JwtAuthenticationUtil.getCurrentUser();
    Integer userId = user.getId();
    Integer result = bGroupMemberMapper.delete(new QueryWrapper<BGroupMember>().eq("user_id", userId).eq("group_id", groupId));
    if (result == 0) return Res.fail("你不属于这个小组");
    return Res.ok("成功退出小组");
  }
}
