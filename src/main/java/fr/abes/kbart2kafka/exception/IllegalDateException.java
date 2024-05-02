package fr.abes.kbart2kafka.exception;

public class IllegalDateException extends Exception {
    public IllegalDateException(Exception e) {
        super(e);
    }

    public IllegalDateException(String message) {
        super(message);
    }
}
