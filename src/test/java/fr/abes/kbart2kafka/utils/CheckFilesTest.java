package fr.abes.kbart2kafka.utils;

import fr.abes.kbart2kafka.exception.IllegalFileFormatException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.ThreadContext;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class CheckFilesTest {

    File file;
    File file2;
    File file3;

    @AfterEach
    public void cleanUp() {
        if(file != null){file.delete();}    // ne pas supprimer. Indispensable pour que les TU fonctionnent.
        if(file2 != null){file2.delete();}  // ne pas supprimer. Indispensable pour que les TU fonctionnent.
        if(file3 != null){file3.delete();}  // ne pas supprimer. Indispensable pour que les TU fonctionnent.
    }

    @Test
    void isFileWithTSVExtension() throws IllegalFileFormatException {
        this.file = new File("test.tsv");
        CheckFiles.isFileWithTSVExtension(file);

        this.file2 = new File("");
        IllegalFileFormatException erreur2 = Assertions.assertThrows(IllegalFileFormatException.class, () -> CheckFiles.isFileWithTSVExtension(file2));
        Assertions.assertEquals("Le nom du fichier est vide", erreur2.getMessage());

        this.file3 = new File("test2.csv");
        IllegalFileFormatException erreur3 = Assertions.assertThrows(IllegalFileFormatException.class, () -> CheckFiles.isFileWithTSVExtension(file3));
        Assertions.assertEquals("le fichier n'est pas au format tsv", erreur3.getMessage());
    }

    @Test
    void detectFileName() throws IllegalFileFormatException {
        this.file = new File("test_test_test_test1_1234-12-12.tsv");
        CheckFiles.detectFileName(file);

        this.file2 = new File("test_test_test_test1_1234-12-12_FORCE.tsv");
        CheckFiles.detectFileName(file2);

        for(String name : Lists.newArrayList("123", "test_1234-12-12.tsv", "test_test_134-12-12.tsv", "test_test_1344-12-12.tsvf", "test_test_1344-12-123.tsv","test_test_test_test_1234/12/12.tsv")) {
            this.file3 = new File(name);
            IllegalFileFormatException erreur2 = Assertions.assertThrows(IllegalFileFormatException.class, () -> CheckFiles.detectFileName(file3));
            Assertions.assertEquals("Le nom du fichier " + name + " n'est pas correct", erreur2.getMessage());
        }
    }

    @Test
    void detectTabulations() throws IOException, IllegalFileFormatException {
        this.file = new File("test.tsv");
        FileUtils.writeStringToFile(file, "test\ttest\ttest", StandardCharsets.UTF_8, true);
        CheckFiles.detectTabulations(file);

        this.file2 = new File("test2.tsv");
        FileUtils.writeStringToFile(file2, "test;test;test", StandardCharsets.UTF_8, true);
        IllegalFileFormatException erreur = Assertions.assertThrows(IllegalFileFormatException.class, () -> CheckFiles.detectTabulations(file2));
        Assertions.assertEquals("Le fichier ne contient pas de tabulation", erreur.getMessage());
    }

    @Test
    void detectOfHeaderPresence() throws IOException, IllegalFileFormatException {
        this.file = new File("test.tsv");
        FileUtils.writeStringToFile(file, "test\ttest\ttest", StandardCharsets.UTF_8, true);
        CheckFiles.detectHeaderPresence("test", file);

        this.file2 = new File("test2.tsv");
        FileUtils.writeStringToFile(file2, "toto\ttata\ttiti", StandardCharsets.UTF_8, true);
        IllegalFileFormatException erreur = Assertions.assertThrows(IllegalFileFormatException.class, () -> CheckFiles.detectHeaderPresence("test", file2));
        Assertions.assertEquals("L'en tete du fichier est incorrecte.", erreur.getMessage());
    }
}
