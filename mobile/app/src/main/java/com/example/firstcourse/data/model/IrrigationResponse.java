package com.example.firstcourse.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response from the backend after a manual irrigation request.
 * This is a simple POJO to capture the success message.
 */
public class IrrigationResponse {

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
