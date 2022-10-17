package com.soku.rebotcorner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class ReBotcornerApplication {
	public static void main(String[] args) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println(1000);
			}
		}, 10000);
		SpringApplication.run(ReBotcornerApplication.class, args);
	}
}
