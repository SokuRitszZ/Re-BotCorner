package com.soku.rebotcorner.controller.lang;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soku.rebotcorner.mapper.LangMapper;
import com.soku.rebotcorner.pojo.Lang;
import com.soku.rebotcorner.utils.NewRes;
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

  @GetMapping("/api/lang/getall")
  public JSONObject getAll() {
    List<Lang> langs = langMapper.selectList(null);

    return NewRes.ok(new JSONObject().set("langs", langs));
  }
}
