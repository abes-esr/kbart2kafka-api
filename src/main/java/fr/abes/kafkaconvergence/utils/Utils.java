package fr.abes.kafkaconvergence.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class Utils {
    public static String extractDomainFromUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String host = uri.getHost();
        if (host == null) {
            throw new URISyntaxException(url, "Format d'URL incorrect");
        }
        return host;
    }
}
