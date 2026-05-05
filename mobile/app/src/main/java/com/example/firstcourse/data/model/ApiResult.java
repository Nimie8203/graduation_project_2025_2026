package com.example.firstcourse.data.model;

public class ApiResult<T> {

    public enum FailureReason {
        NOT_CONNECTED,
        DEVICE_NOT_RESPONDING,
        SERVER_ERROR,
        PARSE_ERROR,
        UNKNOWN
    }

    private final boolean success;
    private final T data;
    private final String message;
    private final FailureReason failureReason;

    private ApiResult(boolean success, T data, String message, FailureReason failureReason) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.failureReason = failureReason;
    }

    public static <T> ApiResult<T> success(T data, String message) {
        return new ApiResult<>(true, data, message, null);
    }

    public static <T> ApiResult<T> error(String message, FailureReason failureReason) {
        return new ApiResult<>(false, null, message, failureReason);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }
}