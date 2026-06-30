package io.github.sh1bari.springa2a.examples.springboot.rest.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SpringBootRestClientExampleProperties.class)
public class SpringBootRestClientExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRestClientExampleApplication.class, args);
    }
}
