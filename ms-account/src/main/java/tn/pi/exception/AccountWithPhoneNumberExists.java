package tn.pi.exception;

public class AccountWithPhoneNumberExists extends RuntimeException {
    public AccountWithPhoneNumberExists(String message) {
        super(message);
    }
}
