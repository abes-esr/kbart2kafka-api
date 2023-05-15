package fr.abes.kafkaconvergence.service;

import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import jakarta.mail.MessagingException;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.util.List;

public interface EmailService {
    void sendMailWithAttachment(String packageName,  List<LigneKbartDto> dataLines) throws MessagingException, NoSuchFileException, DirectoryNotEmptyException;
}
