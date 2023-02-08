package com.griddynamics.internship.stonksjh;

import com.griddynamics.internship.stonksjh.properties.FinnhubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FinnhubProperties.class)
public class StonksJhApplication {

    public static void main(String[] args) {
        SpringApplication.run(StonksJhApplication.class, args);
    }

}
