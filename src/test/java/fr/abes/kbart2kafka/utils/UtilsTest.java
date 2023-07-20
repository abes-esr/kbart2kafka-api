package fr.abes.kbart2kafka.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

class UtilsTest {
    @Test
    void extractDomainFromUrlTest1() throws URISyntaxException {
        String url = "https://www.doi.org/test";
        Assertions.assertEquals("www.doi.org", Utils.extractDomainFromUrl(url));
    }

    @Test
    void extractDomainFromUrlTest2() throws URISyntaxException {
        String url = "http://www.doi.org/test";
        Assertions.assertEquals("www.doi.org", Utils.extractDomainFromUrl(url));
    }

    @Test
    void extractDomainFromUrlTest3() {
        String url = "teskljgfklj/test";
        Assertions.assertThrows(URISyntaxException.class, () -> Utils.extractDomainFromUrl(url));
    }
}
