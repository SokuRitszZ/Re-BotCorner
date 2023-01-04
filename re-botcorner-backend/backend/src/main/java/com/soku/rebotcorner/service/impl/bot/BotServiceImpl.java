package com.soku.rebotcorner.service.impl.bot;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.service.bot.BotService;
import com.soku.rebotcorner.utils.GetUserByToken;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BotServiceImpl implements BotService {
  @Autowired
  private BotMapper mapper;

  @Override
  public JSONObject addBot(JSONObject data) {
    try {
      User user = GetUserByToken.get();

      // title
      String title = data.getStr("title", "").trim();
      if (title.isEmpty()) {
        return NewRes.fail("名称为空");
      }
      if (title.length() > 32) {
        return NewRes.fail("名称长度超过32");
      }

      // gameId
      Integer gameId = data.getInt("gameId");
      if (gameId == null || gameId == 0) {
        return NewRes.fail("没有选择所属游戏");
      }

      // langId
      Integer langId = data.getInt("langId");
      if (langId == null || langId == 0) {
        return NewRes.fail("没有选择所属语言");
      }

      // description
      String description = data.getStr("description", "").trim();
      if (description.isEmpty()) description = "这个人很懒，什么都没留下。";
      if (description.length() > 128) {
        return NewRes.fail("描述长度超过128");
      }

      // code
      String code = data.getStr("code", "").trim();
      Date now = new Date();
      Bot bot = new Bot(
        null,
        user.getId(),
        title,
        description,
        code,
        gameId,
        langId,
        now,
        now,
        true
      );
      RunningBot runningBot = new RunningBot();
      runningBot.setBot(bot);
      runningBot.start();
      JSONObject json = new JSONObject(runningBot.compile());
      if (!"ok".equals(json.getStr("result"))) {
        runningBot.stop();
        return NewRes.fail("编译失败");
      }
      runningBot.stop();

      mapper.insert(bot);

      JSONObject ret = new JSONObject();
      ret.set("id", bot.getId());
      ret.set("createTime", bot.getCreateTime());
      ret.set("modifyTime", bot.getModifyTime());

      return NewRes.ok(ret);
    } catch (Exception e) {
      return NewRes.fail("Token无效");
    }
  }

  @Override
  public JSONObject deleteBot(Integer id) {
    try {
      UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      User user = loginUser.getUser();

      QueryWrapper<Bot> qw = new QueryWrapper<>();
      Bot bot = mapper.selectById(id);
      if (bot == null) {
        return NewRes.fail("不存在此Bot");
      }
      if (bot.getUserId() != user.getId()) {
        return NewRes.fail("没有权限删除此Bot");
      }
      mapper.deleteById(id);

      return NewRes.ok();
    } catch (Exception e) {
      return NewRes.fail("Token无效");
    }
  }

  @Override
  public JSONObject getAll() {
    try {
      UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      User user = loginUser.getUser();

      List<JSONObject> bots = mapper.getDetailBotById(user.getId());
      return NewRes.ok(new JSONObject().set("bots", bots));
    } catch (Exception e) {
      return NewRes.fail("Token 无效");
    }
  }

  @Override
  public JSONObject getCode(int id) {
    try {
      Bot bot = mapper.selectById(id);
      return NewRes.ok(new JSONObject().set("code", bot.getCode()));
    } catch (Exception e) {
      return NewRes.fail("不存在此Bot");
    }
  }

  @Override
  public JSONObject updateBot(JSONObject data) {
    try {
      UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      User user = loginUser.getUser();
      Integer botId = data.getInt("id");

      Map<String, String> map = new HashMap<>();
      Bot bot = mapper.selectById(botId);
      if (bot == null) {
        return NewRes.fail("不存在此Bot");
      }
      if (!bot.getUserId().equals(user.getId())) {
        return NewRes.fail("没有权限修改此Bot");
      }

      String title = data.getStr("title", "").trim();
      String description = data.getStr("description", "").trim();
      String code = data.getStr("code", "").trim();

      if (!title.isEmpty()) {
        if (title.length() > 32) {
          return NewRes.fail("名称长度超过32");
        }
        bot.setTitle(title);
      }

      if (!description.isEmpty()) {
        if (description.length() > 128) {
          return NewRes.fail("描述长度超过128");
        }
        bot.setDescription(description);
      }

      if (!code.isEmpty()) {
        if (code.length() > 8196) {
          return NewRes.fail("代码长度超过8196");
        }
        bot.setCode(code);
        RunningBot runningBot = new RunningBot();
        runningBot.setBot(bot);
        runningBot.start();
        JSONObject json = new JSONObject(runningBot.compile());
        runningBot.stop();
        String result = json.getStr("result");
        if (!"ok".equals(result)) {
          return NewRes.fail("编译失败");
        }
      }
      bot.setModifyTime(new Date());

      mapper.updateById(bot);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));

      JSONObject ret = new JSONObject();
      ret.set("modifyTime", sdf.format(bot.getModifyTime()));
      return NewRes.ok(ret);
    } catch (Exception e) {
      System.out.println("e = " + e);
      return NewRes.fail("Token无效");
    }
  }

  @Override
  public JSONObject changeVisible(int id, boolean visible) {
    try {
      User user = GetUserByToken.get();
      try {
        mapper.changeVisible(user.getId(), id, visible);
        return NewRes.ok(new JSONObject().set("message", "修改成功"));
      } catch (Exception e) {
        return NewRes.fail("没有权限");
      }
    } catch (Exception e) {
      return NewRes.fail("Token无效");
    }
  }
}
