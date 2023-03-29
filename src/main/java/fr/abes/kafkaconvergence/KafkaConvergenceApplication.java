package fr.abes.kafkaconvergence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.client.RestTemplate;

@EnableKafka
@SpringBootApplication
public class KafkaConvergenceApplication {
	public static void main(String[] args) {
		SpringApplication.run(KafkaConvergenceApplication.class, args);
	}

}
