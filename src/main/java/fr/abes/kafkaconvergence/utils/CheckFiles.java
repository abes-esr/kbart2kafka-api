package fr.abes.kafkaconvergence.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CheckFiles {
    /**
     * Controle si le fichier à bien une extension tsv
     * @param file fichier en entrée
     * @return true si extension présente, false sinon
     * @throws IOException erreur avec le fichier en entrée
     */
    public static boolean isFileWithTSVExtension(MultipartFile file) throws IOException {
        //Filename extension control
        String fileName = file.getOriginalFilename(); // get file name
        if (fileName == null || fileName.isEmpty()) return false; // check if file name is valid
        String[] parts = fileName.split("\\."); // split by dot
        String extension = parts[parts.length - 1]; // get last part as extension

        return extension.equalsIgnoreCase("tsv"); // compare with tsv ignoring case
    }

    /**
     * Détecte si le fichier présente des tabulations
     * @param file fichier en entrée
     * @return true si des tabulations sont présentes dans le fichier, false sinon
     * @throws IOException erreur avec le fichier en entrée
     */
    public static boolean detectTabulations(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("\t")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Détecte la présence d'une entête dans le fichier
     * @param header terme à recherche dans l'entête
     * @param file fichier en entrée
     * @return true si le terme est présent
     * @throws IOException
     */
    public static boolean detectOfHeaderPresence(String header, MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(header)) {
                    return true;
                }
            }
        }
        return false;
    }
}
