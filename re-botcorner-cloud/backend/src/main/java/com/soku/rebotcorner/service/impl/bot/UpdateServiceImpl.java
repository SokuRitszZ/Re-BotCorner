package com.soku.rebotcorner.service.impl.bot;

import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.bot.UpdateService;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateServiceImpl implements UpdateService {

  @Autowired
  private BotMapper botMapper;

  @Override
  public Map<String, String> updateBot(Map<String, String> data) {
    UsernamePasswordAuthenticationToken authenticate = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
    User user = loginUser.getUser();
    Integer botId = Integer.parseInt(data.get("id"));
    Map<String, String> map = new HashMap<>();
    Bot bot = botMapper.selectById(botId);
    if (bot == null) {
      map.put("result", "不存在此Bot");
      return map;
    }
    if (!bot.getUserId().equals(user.getId())) {
      map.put("result", "没有权限修改此Bot");
      return map;
    }

    String title = data.get("title");
    String description = data.get("description");
    String code = data.get("code");

    if (title != null && title.length() != 0) {
      if (title.length() > 8) {
        map.put("result", "名字超过8个字");
        return map;
      }
      bot.setTitle(title);
    }

    if (description != null && description.length() > 0) {
      if (description.length() > 128) {
        map.put("result", "描述超过128个字");
        return map;
      }
      bot.setDescription(description);
    }

    if (code != null && code.length() > 0) {
      if (code.length() > 8196) {
        map.put("result", "代码超过8196个字");
        return map;
      }
      bot.setCode(code);
    }
    bot.setModifyTime(new Date());

    botMapper.updateById(bot);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    map.put("result", "success");
    map.put("modifyTime", sdf.format(bot.getModifyTime()));
    return map;
  }
}
