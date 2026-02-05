package com.nect.api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(basePackages = "com.nect")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.nect")
@EntityScan(basePackages = "com.nect")
@EnableAsync
@EnableScheduling
public class NectApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NectApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
