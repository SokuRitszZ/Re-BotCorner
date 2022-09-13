package com.soku.rebotcorner.service.impl.bot;

import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.pojo.Bot;
import com.soku.rebotcorner.pojo.User;
import com.soku.rebotcorner.service.bot.AddService;
import com.soku.rebotcorner.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.metadata.OracleTableMetaDataProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {

  @Autowired
  private BotMapper botMapper;

  @Override
  public Map<String, String> addBot(Map<String, String> data) {
    UsernamePasswordAuthenticationToken authenticate =
      (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
    User user = loginUser.getUser();

    String title = data.get("title");
    String description = data.get("description");
    String code = data.get("code");
    Integer gameId = Integer.parseInt(data.get("gameId"));
    Integer langId = Integer.parseInt(data.get("langId"));
    Map<String, String> map = new HashMap<>();

    if (title == null || title.length() == 0) {
      map.put("result", "名字为空");
      return map;
    } else if (title.length() > 8) {
      map.put("result", "名字长度大于8");
      return map;
    } else if (description != null && description.length() > 128) {
      map.put("result", "描述大于128");
      return map;
    } else if (code == null || code.length() == 0) {
      map.put("result", "代码为空");
      return map;
    } else if (code.length() > 8196) {
      map.put("result", "代码超过8196字");
      return map;
    } else if (gameId == -1) {
      map.put("result", "没有指定游戏");
      return map;
    } else if (langId == -1) {
      map.put("result", "没有指定语言");
      return map;
    }

    if (description == null || description.length() == 0) {
      description = "这个人很懒，什么都没有留下。";
    }

    Date now = new Date();
    Bot bot = new Bot(null, user.getId(), title, description, code, 1500, gameId, langId, now, now, false);
    botMapper.insert(bot);

    map.put("result", "success");
    map.put("id", bot.getId().toString());
    map.put("userId", bot.getUserId().toString());
    map.put("description", bot.getDescription());
    map.put("rating", bot.getRating().toString());
    String timeFMT = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(timeFMT);
    map.put("createTime", sdf.format(bot.getCreateTime()));
    map.put("modifyTime", sdf.format(bot.getModifyTime()));

    return map;
  }
}
