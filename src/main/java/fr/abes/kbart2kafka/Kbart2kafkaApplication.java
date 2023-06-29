package fr.abes.kbart2kafka;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class Kbart2kafkaApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Kbart2kafkaApplication.class, args);

	}
	@Override
	public void run(String... args) throws Exception {
		// chercher le fichier/Lire
		// CheckFiles.verifyFile();
		//START
			//ligne par ligne injecter dans le topic
			//Mapper ligne en ligneKbartDto
		//END
	}
}
