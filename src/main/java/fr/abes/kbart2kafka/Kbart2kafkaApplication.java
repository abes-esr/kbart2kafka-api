package fr.abes.kbart2kafka;

import fr.abes.kbart2kafka.exception.IllegalFileFormatException;
import fr.abes.kbart2kafka.utils.CheckFiles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

import java.io.File;
import java.io.IOException;

@EnableKafka
@SpringBootApplication
public class Kbart2kafkaApplication implements CommandLineRunner {

	@Value("${file.header}")
	private String header;

	public static void main(String[] args) {
		SpringApplication.run(Kbart2kafkaApplication.class, args);

	}
	@Override
	public void run(String... args) throws IOException {

		//	Contrôle de la présence d'un paramètre au lancement de Kbart2kafkaApplication
		if(args.length == 0 || args[0] == null || args[0].trim().isEmpty()) {
			System.out.println("Il faut passer un chemin d'accès");
		} else {
			//	Récupération du chemin d'accès au fichier
			File CP_file = new File(args[0]);

			try {
				//	Appelle du service de vérification de fichier
				CheckFiles.verifyFile(CP_file, header);

				// TODO : injecter dans le topic ligne par ligne (mapper en ligneKbartDto avant l'envoi) via l'appel au TopicProducer

			} catch (Exception e) {
				throw new IOException(e);
			} catch (IllegalFileFormatException e) {
				throw new RuntimeException(e);
			}

		}
	}
}
