package com.soku.matchingsystem;

import com.soku.matchingsystem.pools.MatchPool;
import com.soku.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReBotcornerMatchingSystemApplication {
  public static void main(String[] args) {
    SpringApplication.run(ReBotcornerMatchingSystemApplication.class, args);
  }
}
