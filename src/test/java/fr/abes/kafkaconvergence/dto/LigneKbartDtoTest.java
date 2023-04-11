package fr.abes.kafkaconvergence.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LigneKbartDtoTest {

    @Test
    void toStringTest() {
        LigneKbartDto ligne = new LigneKbartDto();
        ligne.setOnline_identifier("");
        ligne.setPrint_identifier("");
        ligne.setPublication_title("test");
        ligne.setPublication_type("monograph");

        Assertions.assertEquals("publication title : test / publication_type : monograph", ligne.toString());

        ligne.setOnline_identifier("11111111");
        Assertions.assertEquals("publication title : test / publication_type : monograph / online_identifier : 11111111", ligne.toString());

        ligne.setPrint_identifier("987123456789");
        Assertions.assertEquals("publication title : test / publication_type : monograph / online_identifier : 11111111 / print_identifier : 987123456789", ligne.toString());

        ligne.setOnline_identifier("");
        Assertions.assertEquals("publication title : test / publication_type : monograph / print_identifier : 987123456789", ligne.toString());

    }
}
