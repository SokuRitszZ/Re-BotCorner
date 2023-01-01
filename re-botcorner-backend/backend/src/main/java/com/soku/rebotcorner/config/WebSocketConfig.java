package com.soku.rebotcorner.config;

import com.soku.rebotcorner.mapper.*;
import com.soku.rebotcorner.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Autowired
  public void setWebSocketBean(
    RecordMapper recordMapper,
    UserMapper userMapper,
    BotMapper botMapper,
    RestTemplate restTemplate,
    LangMapper langMapper,
    RatingMapper ratingMapper,
    StringRedisTemplate redis
  ) {
    RecordDAO.mapper = recordMapper;
    UserDAO.mapper = userMapper;
    BotDAO.mapper = botMapper;
    RT.restTemplate = restTemplate;
    LangDAO.langMapper = langMapper;
    RatingDAO.mapper = ratingMapper;
    CacheClient.redis = redis;
  }
}