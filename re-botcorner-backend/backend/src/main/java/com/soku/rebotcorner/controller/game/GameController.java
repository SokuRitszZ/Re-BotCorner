package com.soku.rebotcorner.controller.game;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soku.rebotcorner.mapper.GameMapper;
import com.soku.rebotcorner.pojo.Game;
import com.soku.rebotcorner.utils.NewRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.soku.rebotcorner.utils.RedisConstants.GAME_KEY;
import static com.soku.rebotcorner.utils.RedisConstants.GAME_TTL;

@RestController
public class GameController {
  @Autowired
  private StringRedisTemplate redis;

  @Autowired
  private GameMapper gameMapper;

  @GetMapping("/api/game/getall")
  public JSONObject getAll() {
    List<Game> games = gameMapper.selectList(null);
    return NewRes.ok(new JSONObject().set("games", games));
  }
}
