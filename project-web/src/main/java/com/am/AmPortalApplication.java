package com.am;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AmPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmPortalApplication.class, args);
    }
}
