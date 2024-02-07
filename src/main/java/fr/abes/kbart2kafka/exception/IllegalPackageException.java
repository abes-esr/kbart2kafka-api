package fr.abes.kbart2kafka.exception;

public class IllegalPackageException extends Throwable {
    public IllegalPackageException(Exception e) {
        super(e);
    }

    public IllegalPackageException(String s) {
        super(s);
    }
}
