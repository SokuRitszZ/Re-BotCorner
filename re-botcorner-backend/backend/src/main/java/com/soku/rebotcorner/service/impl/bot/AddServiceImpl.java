package com.soku.rebotcorner.service.impl.bot;

import cn.hutool.json.JSONObject;
import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.runningbot.RunningBot;
import com.soku.rebotcorner.service.bot.AddService;
import com.soku.rebotcorner.utils.NewRes;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AddServiceImpl implements AddService {

  @Autowired
  private BotMapper botMapper;

  @Override
  public JSONObject addBot(JSONObject data) {
    try {
      UsernamePasswordAuthenticationToken authenticate =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
      User user = loginUser.getUser();

      // title
      String title = data.getStr("title", "").trim();
      if (title.isEmpty()) { return NewRes.fail("名称为空"); }
      if (title.length() > 32) { return NewRes.fail("名称长度超过32"); }

      // gameId
      Integer gameId = data.getInt("gameId");
      if (gameId == null || gameId == 0) { return NewRes.fail("没有选择所属游戏"); }

      // langId
      Integer langId = data.getInt("langId");
      if (langId == null || langId == 0) { return NewRes.fail("没有选择所属语言"); }

      // description
      String description = data.getStr("description", "").trim();
      if (description.isEmpty()) description = "这个人很懒，什么都没留下。";
      if (description.length() > 128) { return NewRes.fail("描述长度超过128"); }

      // code
      String code = data.getStr("code", "").trim();
      Date now = new Date();
      Bot bot = new Bot(
        null,
        user.getId(),
        title,
        description,
        code,
        1500,
        gameId,
        langId,
        now,
        now,
        false
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

      botMapper.insert(bot);

      JSONObject ret = new JSONObject();
      ret.set("id", bot.getId());
      ret.set("createTime", bot.getCreateTime());
      ret.set("modifyTime", bot.getModifyTime());

      return NewRes.ok(ret);
    } catch (Exception e) {
      return NewRes.fail("Token无效");
    }
  }
}
