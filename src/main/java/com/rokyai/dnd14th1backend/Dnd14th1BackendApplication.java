package com.rokyai.dnd14th1backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Dnd14th1BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dnd14th1BackendApplication.class, args);
    }
}
