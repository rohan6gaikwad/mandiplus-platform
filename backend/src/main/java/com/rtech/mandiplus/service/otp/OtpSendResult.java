package com.rtech.mandiplus.service.otp;

public class OtpSendResult {
    private final boolean success;
    private final String messageId;
    private final String errorMessage;

    public OtpSendResult(boolean success, String messageId, String errorMessage) {
        this.success = success;
        this.messageId = messageId;
        this.errorMessage = errorMessage;
    }

    public static OtpSendResult success(String messageId) {
        return new OtpSendResult(true, messageId, null);
    }

    public static OtpSendResult failure(String errorMessage) {
        return new OtpSendResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
