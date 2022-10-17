package com.soku.rebotcorner;

import cn.hutool.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class ReBotcornerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReBotcornerApplication.class, args);
	}
}
