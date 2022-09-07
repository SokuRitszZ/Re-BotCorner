package com.soku.rebotcorner.config;

import com.soku.rebotcorner.games.util.RecordDAO;
import com.soku.rebotcorner.mapper.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Autowired
  public void setWebSocketBean(RecordMapper recordMapper){
    RecordDAO.recordMapper = recordMapper;
  }
}