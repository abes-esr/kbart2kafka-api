package fr.abes.kafkaconvergence.utils;

import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class Utils {

    public static String extractDomainFromUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String host = uri.getHost();
        if (host == null) {
            throw new URISyntaxException(url, "Format d'URL incorrect");
        }
        return host;
    }

    public static String extractDOI(LigneKbartDto kbart) {
        String doiPattern = "doi.org/(?<doi>10.\\d{0,15}.\\d{0,15}.+)";

        if (kbart.getTitle_url() != null && !kbart.getTitle_url().isEmpty()){
            return Pattern.compile(doiPattern).matcher(kbart.getTitle_url()).find() ? kbart.getTitle_url().split("doi.org/")[kbart.getTitle_url().split("doi.org/").length - 1] : "";
        }
        if (kbart.getTitle_id() != null && !kbart.getTitle_id().isEmpty()){
            return Pattern.compile(doiPattern).matcher(kbart.getTitle_id()).find() ? kbart.getTitle_id().split("doi.org/")[kbart.getTitle_id().split("doi.org/").length - 1] : "";
        }
        return "";
    }
}
