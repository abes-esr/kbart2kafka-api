package fr.abes.kbart2kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Kbart2kafkaApplication {
    public static void main(String[] args) {
        SpringApplication.run(Kbart2kafkaApplication.class, args);
    }
}
