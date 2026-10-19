package com.rtech.mandiplus.exception;

public class OtpCooldownException extends OtpException {
    private final int remainingSeconds;

    public OtpCooldownException(String message, int remainingSeconds) {
        super(message);
        this.remainingSeconds = remainingSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
