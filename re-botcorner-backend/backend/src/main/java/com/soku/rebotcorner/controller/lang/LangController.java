package com.soku.rebotcorner.controller.lang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soku.rebotcorner.mapper.LangMapper;
import com.soku.rebotcorner.pojo.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.soku.rebotcorner.utils.RedisConstants.LANG_KEY;

@RestController
public class LangController {
  @Autowired
  private LangMapper langMapper;

  @Autowired
  private StringRedisTemplate redis;

  @GetMapping("/api/lang/getAll")
  public List<Lang> getAll() {
    ObjectMapper mapper = new ObjectMapper();
    if (redis.opsForList().size(LANG_KEY) == 0) {
      List<Lang> langs = langMapper.selectList(null);
      for (Lang lang: langs) {
        try {
          String jsonStr = mapper.writeValueAsString(lang);
          redis.opsForList().rightPush("rebc:lang", jsonStr);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    }
    List<String> langStrs = redis.opsForList().range(LANG_KEY, 0, -1);
    List<Lang> langs = new ArrayList<>();
    for (String str: langStrs) {
      try {
        langs.add(mapper.readValue(str, Lang.class));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    return langs;
  }
}
