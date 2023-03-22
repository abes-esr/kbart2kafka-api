package fr.abes.kafkaconvergence.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CheckFilesTest {


    @Test
    void isFileWithTSVExtension() throws IOException {
        MultipartFile file = new MockMultipartFile("test.tsv", "test.tsv", "UTF-8", new byte[]{});
        Assertions.assertTrue(CheckFiles.isFileWithTSVExtension(file));

        file = new MockMultipartFile("test.csv", "test.csv", "UTF-8", new byte[]{});
        Assertions.assertFalse(CheckFiles.isFileWithTSVExtension(file));
    }

    @Test
    void detectTabulations() throws IOException {
        byte[] datas = "test\ttest\ttest".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("test.tsv", "test.tsv", "UTF-8", datas);
        Assertions.assertTrue(CheckFiles.detectTabulations(file));

        datas = "test;test;test".getBytes(StandardCharsets.UTF_8);
        file = new MockMultipartFile("test.tsv", "test.tsv", "UTF-8", datas);
        Assertions.assertFalse(CheckFiles.detectTabulations(file));
    }

    @Test
    void detectOfHeaderPresence() throws IOException {
        byte[] datas = "test\ttest\ttest".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("test.tsv", "test.tsv", "UTF-8", datas);
        Assertions.assertTrue(CheckFiles.detectOfHeaderPresence("test", file));

        datas = "toto\ttata\ttiti".getBytes(StandardCharsets.UTF_8);
        file = new MockMultipartFile("test.tsv", "test.tsv", "UTF-8", datas);
        Assertions.assertFalse(CheckFiles.detectOfHeaderPresence("test", file));
    }
}