package com.example.mafia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@SpringBootApplication
public class MafiaApplication {

  public static void main(String[] args) {
    SpringApplication.run(MafiaApplication.class, args);
  }
}
