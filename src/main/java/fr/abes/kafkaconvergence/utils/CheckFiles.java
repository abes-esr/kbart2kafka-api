package fr.abes.kafkaconvergence.utils;

import fr.abes.kafkaconvergence.exception.IllegalFileFormatException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CheckFiles {
    /**
     * Controle si le fichier à bien une extension tsv
     *
     * @param file fichier en entrée
     * @return true si extension présente, false sinon
     * @throws IOException erreur avec le fichier en entrée
     */
    public static void isFileWithTSVExtension(MultipartFile file) throws IllegalFileFormatException {
        //Filename extension control
        String fileName = file.getOriginalFilename(); // get file name
        if (fileName == null || fileName.isEmpty())
            throw new IllegalFileFormatException("Le nom du fichier est vide"); // check if file name is valid
        String[] parts = fileName.split("\\."); // split by dot
        String extension = parts[parts.length - 1]; // get last part as extension
        // compare with tsv ignoring case
        if (!extension.equalsIgnoreCase("tsv"))
            throw new IllegalFileFormatException("le fichier n'est pas au format tsv");
    }

    /**
     * Détecte si le fichier présente des tabulations
     *
     * @param file fichier en entrée
     * @return true si des tabulations sont présentes dans le fichier, false sinon
     * @throws IOException erreur avec le fichier en entrée
     */
    public static void detectTabulations(MultipartFile file) throws IOException, IllegalFileFormatException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("\t")) {
                    throw new IllegalFileFormatException("Le fichier ne contient pas de tabulation");
                }
            }
        }
    }

    /**
     * Détecte la présence d'une entête dans le fichier
     *
     * @param header terme à recherche dans l'entête
     * @param file   fichier en entrée
     * @return true si le terme est présent
     * @throws IOException
     */
    public static void detectOfHeaderPresence(String header, MultipartFile file) throws IOException, IllegalFileFormatException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line = reader.readLine();
            if (!line.contains(header))
                throw new IllegalFileFormatException("Le champ " + header + " est absent de l'en tête du fichier");
        }
    }


    public static String getProviderFromFilename(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String[] filenameFields = filename.split("_");
        if (filenameFields.length > 0)
            return filenameFields[0].toLowerCase();
        return "";
    }

    public static void verifyFile(MultipartFile file, String header) throws IllegalFileFormatException, IOException {
        CheckFiles.isFileWithTSVExtension(file);
        CheckFiles.detectTabulations(file);
        CheckFiles.detectOfHeaderPresence(header, file);
    }
}
