package fr.abes.kbart2kafka.exception;

public class IllegalProviderException extends Exception {
    public IllegalProviderException(String message) {
        super(message);
    }

    public IllegalProviderException(Exception e) { super(e); }
}
