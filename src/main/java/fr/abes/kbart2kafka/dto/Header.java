package fr.abes.kbart2kafka.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Header {

    private String fileName;

    private long totalNumberOfLine;

    private int currentLine;

    public Header(String fileName, long totalNumberOfLine) {
        this.fileName = fileName;
        this.totalNumberOfLine = totalNumberOfLine;
    }
}
