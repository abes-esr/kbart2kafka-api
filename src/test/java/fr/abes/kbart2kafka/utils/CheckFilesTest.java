package fr.abes.kbart2kafka.utils;

import fr.abes.kbart2kafka.exception.IllegalFileFormatException;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class CheckFilesTest {

    File file;
    File file2;
    File file3;

    @AfterEach
    public void cleanUp() {
        if(file != null){file.delete();}
        if(file2 != null){file2.delete();}
        if(file3 != null){file3.delete();}
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
        File file = new File("test_test_test_test1_1234-12-12.tsv");

        CheckFiles.detectFileName(file);

        for(String name : Lists.newArrayList("123", "test_1234-12-12.tsv", "test_test_134-12-12.tsv", "test_test_1344-12-12.tsvf", "test_test_1344-12-123.tsv")) {
            File file2 = new File(name);
            IllegalFileFormatException erreur2 = Assertions.assertThrows(IllegalFileFormatException.class, () -> CheckFiles.detectFileName(file2));
            Assertions.assertEquals("Le nom du fichier " + name + " n'est pas correcte", erreur2.getMessage());
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
        Assertions.assertEquals("Le champ test est absent de l'en tête du fichier", erreur.getMessage());
    }
}
