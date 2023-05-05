package fr.abes.kafkaconvergence.service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public void sendMailWithAttachment(String packageName, List<LigneKbartDto> dataLines) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            //  Création du message
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(this.sender);
            mimeMessageHelper.setTo(this.sender);
            mimeMessageHelper.setText("");
            mimeMessageHelper.setSubject("Rapport de traitement BestPPN " + packageName);

            //  Création du fichier
            Path csvPath = Path.of("Rapport de traitement BestPPN " + packageName + ".csv");
            Writer writer = Files.newBufferedWriter(csvPath);

            //  Création du header
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            String[] header = { "publication_title", "print_identifier", "online_identifier", "date_first_issue_online", "num_first_vol_online", "num_first_issue_online", "date_last_issue_online", "num_last_vol_online", "num_last_issue_online", "title_url", "first_author", "title_id", "embargo_info", "coverage_depth", "notes", "publisher_name", "publication_type", "date_monograph_published_print", "date_monograph_published_online", "monograph_volume", "monograph_edition", "first_editor", "parent_publication_title_id", "preceding_publication_title_id", "access_type", "bestPpn", "errorType" };
            csvWriter.writeNext(header);

            //  Création du beanToCsvBuilder avec le writer de type LigneKbartDto.class
            StatefulBeanToCsvBuilder<LigneKbartDto> builder = new StatefulBeanToCsvBuilder<>(writer);
            StatefulBeanToCsv<LigneKbartDto> beanWriter = builder.build();

            //  Peuple le fichier csv avec les données
            beanWriter.write(dataLines);

            //  Ferme le Writer
            writer.close();

            //  Récupérer le fichier csv
            FileSystemResource file = new FileSystemResource("Rapport de traitement BestPPN " + packageName + ".csv");

            //  Attache le fichier au message
            mimeMessageHelper.addAttachment("Rapport de traitement BestPPN " + packageName + ".csv", file);

            //  Envoi du message par mail
            javaMailSender.send(mimeMessage);

            //  Suppression du csv temporaire
            Files.deleteIfExists(csvPath);

        } catch (MessagingException e) {
            throw new MessagingException("Une erreur est survenue lors de l'envoi du mail. Veuillez réessayer. Détails : " + e);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        }
    }
}
