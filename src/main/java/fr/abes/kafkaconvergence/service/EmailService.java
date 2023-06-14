package fr.abes.kafkaconvergence.service;

import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PackageKbartDto;
import jakarta.mail.MessagingException;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.util.List;

public interface EmailService {
    void sendMailWithAttachment(String packageName,  PackageKbartDto dataLines) throws MessagingException, NoSuchFileException, DirectoryNotEmptyException;
}
