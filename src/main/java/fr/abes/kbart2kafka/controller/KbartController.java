package fr.abes.kbart2kafka.controller;

import fr.abes.kbart2kafka.exception.IllegalDateException;
import fr.abes.kbart2kafka.exception.IllegalPackageException;
import fr.abes.kbart2kafka.exception.IllegalProviderException;
import fr.abes.kbart2kafka.service.FileService;
import fr.abes.kbart2kafka.service.ProviderPackageService;
import fr.abes.kbart2kafka.utils.CheckFiles;
import fr.abes.kbart2kafka.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
@Slf4j
public class KbartController {
    @Value("${kbart.header}")
    private String kbartHeader;
    @Value("${abes.pathToKbart}")
    private String pathToKbart;
    private final FileService fileService;

    private final ProviderPackageService providerPackageService;

    public KbartController(FileService fileService, ProviderPackageService providerPackageService) {
        this.fileService = fileService;
        this.providerPackageService = providerPackageService;
    }

    @PostMapping(value = "/uploadFile/{fileName}")
    public void uploadFile(@PathVariable String fileName) {
        long startTime = System.currentTimeMillis();
        //	Contrôle de la présence d'un paramètre au lancement de Kbart2kafkaApplication
        if (fileName == null || fileName.isEmpty()) {
            log.error("Message envoyé : {}", "Le chemin d'accès au fichier tsv n'a pas été trouvé dans les paramètres de l'application");
        } else {
            ThreadContext.put("package", fileName);
            log.info("Debut envois kafka de : {}", fileName);
            //	Récupération du chemin d'accès au fichier
            File tsvFile = new File(pathToKbart + fileName);
            try {
                CheckFiles.verifyFile(tsvFile, kbartHeader);
                checkExistingPackage(tsvFile.getName());
                fileService.loadFile(tsvFile);
            } catch (Exception | IllegalPackageException e) {
                log.error(e.getMessage());
                log.info("Traitement refusé du fichier {}", tsvFile.getName());
            }
        }
        long endTime = System.currentTimeMillis();
        double executionTime = (double) (endTime - startTime) / 1000;
        log.debug("Temps d'exécution : {} secondes", executionTime);
    }

    private void checkExistingPackage(String filename) throws IllegalProviderException, IllegalPackageException, IllegalDateException {
        if (providerPackageService.hasMoreRecentPackageInBdd(Utils.extractProvider(filename), Utils.extractPackageName(filename), Utils.extractDateFilename(filename)))
            throw new IllegalPackageException("Un package plus récent est déjà présent dans la base");
    }
}
