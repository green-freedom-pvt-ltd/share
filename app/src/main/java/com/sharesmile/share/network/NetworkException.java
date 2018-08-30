package com.sharesmile.share.network;

/**
 Custom Exception subclass which consolidates all exceptions arising due to networking
 */
public class NetworkException extends Exception {

    private static final String TAG = "NetworkException";
    public static final int HTTP_STATUS_NOT_AVAILABLE = -1;
    private String errorMessage;
    private int failureType;
    private int httpStatusCode;
    private String messageFromServer;
    private String errorResponse;

    private NetworkException(Throwable cause, int httpStatusCode, String errorMessage,
                             String messageFromServer, int failureType, String errorResponse) {
        super(errorMessage, cause);
        this.errorMessage = errorMessage;
        this.httpStatusCode = httpStatusCode;
        this.messageFromServer = messageFromServer;
        this.failureType = failureType;
        this.errorResponse = errorResponse;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }

    public String getMessageFromServer() {
        return messageFromServer;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public int getFailureType(){
        return failureType;
    }

    public static class Builder {

        private String errorMessage;
        private int httpStatusCode = HTTP_STATUS_NOT_AVAILABLE;
        private String messageFromServer;
        private String errorResponse;
        private Throwable cause;
        private int failureType = FailureType.RESPONSE_FAILURE;

        public Builder() {
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder failureType(int failureType) {
            this.failureType = failureType;
            return this;
        }

        public Builder httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder messageFromServer(String messageFromServer) {
            this.messageFromServer = messageFromServer;
            return this;
        }

        public Builder errorResponse(String errorResponse) {
            this.errorResponse = errorResponse;
            return this;
        }

        public Builder cause(Throwable th) {
            this.cause = th;
            return this;
        }

        public NetworkException build() {
            return new NetworkException(cause, httpStatusCode, errorMessage, messageFromServer,
                    failureType, errorResponse);
        }
    }

    @Override
    public String toString() {
        return "NetworkException{" +
                "errorMessage='" + errorMessage + '\'' +
                ", failureType=" + failureType +
                ", httpStatusCode=" + httpStatusCode +
                ", messageFromServer='" + messageFromServer + '\'' +
                ", errorResponse='" + errorResponse + '\'' +
                '}';
    }
}
