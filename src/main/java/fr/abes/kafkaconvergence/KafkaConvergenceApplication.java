package fr.abes.kafkaconvergence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class KafkaConvergenceApplication {
	public static void main(String[] args) {
		SpringApplication.run(KafkaConvergenceApplication.class, args);
	}

}
