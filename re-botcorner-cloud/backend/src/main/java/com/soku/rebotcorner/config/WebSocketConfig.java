package com.soku.rebotcorner.config;

import com.soku.rebotcorner.mapper.BotMapper;
import com.soku.rebotcorner.mapper.LangMapper;
import com.soku.rebotcorner.mapper.UserMapper;
import com.soku.rebotcorner.utils.*;
import com.soku.rebotcorner.mapper.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Autowired
  public void setWebSocketBean(RecordMapper recordMapper, UserMapper userMapper, BotMapper botMapper, RestTemplate restTemplate, LangMapper langMapper){
    RecordDAO.recordMapper = recordMapper;
    UserDAO.userMapper = userMapper;
    BotDAO.botMapper = botMapper;
    RT.restTemplate = restTemplate;
    LangDAO.langMapper = langMapper;
  }
}