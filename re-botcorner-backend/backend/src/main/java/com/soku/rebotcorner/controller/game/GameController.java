package com.soku.rebotcorner.controller.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soku.rebotcorner.mapper.GameMapper;
import com.soku.rebotcorner.pojo.Game;
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

  @GetMapping("/api/game/getAll")
  public List<Game> getAll() {
    ObjectMapper mapper = new ObjectMapper();
    int size = Math.toIntExact(redis.opsForList().size("rebc:game"));
    if (size == 0) {
      List<Game> games = gameMapper.selectList(null);
      for (Game game: games) {
        try {
          String str = mapper.writeValueAsString(game);
          redis.opsForList().rightPush(GAME_KEY, str);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    }

    List<Game> games = new ArrayList<>();
    List<String> gameStrs = redis.opsForList().range(GAME_KEY, 0, -1);
    for (String str: gameStrs) {
      try {
        games.add(mapper.readValue(str, Game.class));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    return gameMapper.selectList(null);
  }
}
