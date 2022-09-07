package com.soku.matchingsystem;

import com.soku.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReBotcornerCloudApplication {
  public static void main(String[] args) {
    MatchingServiceImpl.snakePool.start();
    SpringApplication.run(ReBotcornerCloudApplication.class, args);
  }
}
