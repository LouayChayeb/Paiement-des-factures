package tn.pi.exception;

public class AccountAllReadyException extends RuntimeException {
    public AccountAllReadyException(String message) {
        super(message);
    }
}
