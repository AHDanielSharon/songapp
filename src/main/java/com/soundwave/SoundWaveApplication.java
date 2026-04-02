package com.soundwave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SoundWaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoundWaveApplication.class, args);
    }
}
