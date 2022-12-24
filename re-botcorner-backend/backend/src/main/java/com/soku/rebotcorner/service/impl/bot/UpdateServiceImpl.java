package com.soku.rebotcorner.service.impl.bot;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.service.bot.UpdateService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Service
public class UpdateServiceImpl implements UpdateService {

  @Autowired
  private BotMapper botMapper;

  @Override
  public JSONObject updateBot(JSONObject data) {
    try {
      UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      User user = loginUser.getUser();
      Integer botId = data.getInt("id");

      Map<String, String> map = new HashMap<>();
      Bot bot = botMapper.selectById(botId);
      if (bot == null) { return NewRes.fail("不存在此Bot"); }
      if (!bot.getUserId().equals(user.getId())) {
        return NewRes.fail("没有权限修改此Bot");
      }

      String title = data.getStr("title", "").trim();
      String description = data.getStr("description", "").trim();
      String code = data.getStr("code", "").trim();

      if (!title.isEmpty()) {
        if (title.length() > 32) { return NewRes.fail("名称长度超过32"); }
        bot.setTitle(title);
      }

      if (!description.isEmpty()) {
        if (description.length() > 128) { return NewRes.fail("描述长度超过128"); }
        bot.setDescription(description);
      }

      if (!code.isEmpty()) {
        if (code.length() > 8196) { return NewRes.fail("代码长度超过8196"); }
        bot.setCode(code);
        RunningBot runningBot = new RunningBot();
        runningBot.setBot(bot);
        runningBot.start();
        JSONObject json = new JSONObject(runningBot.compile());
        runningBot.stop();
        String result = json.getStr("result");
        if (!"ok".equals(result)) { return NewRes.fail("编译失败"); }
      }
      bot.setModifyTime(new Date());

      botMapper.updateById(bot);

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
}
