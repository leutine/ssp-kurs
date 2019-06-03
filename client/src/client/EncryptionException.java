package client;

public class EncryptionException extends Exception {

    public EncryptionException() {
    }

    public EncryptionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}