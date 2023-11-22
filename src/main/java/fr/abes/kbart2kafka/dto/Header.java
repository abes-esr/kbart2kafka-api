package fr.abes.kbart2kafka.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Header {

    private String fileName;

    public Header(String fileName) {
        this.fileName = fileName;
    }
}
