package com.rtech.mandiplus.exception;

public class OtpMaxAttemptsExceededException extends OtpException {
    public OtpMaxAttemptsExceededException(String message) {
        super(message);
    }
}
