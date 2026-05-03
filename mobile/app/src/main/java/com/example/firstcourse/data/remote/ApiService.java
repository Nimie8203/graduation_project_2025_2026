package com.example.firstcourse.data.remote;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * This interface defines the REST API endpoints for our application.
 * Retrofit uses this interface to generate the HTTP client.
 * Each method represents one API call, annotated with HTTP methods like @GET or @POST.
 * The return types are wrapped in a Retrofit Call object, which allows for asynchronous
 * or synchronous execution.
 */
public interface ApiService {
    @GET("/api/status")
    Call<JsonObject> getStatus();

    @POST("/api/pump")
    Call<JsonObject> setPumpState(@Body JsonObject body);

    @POST("/api/led")
    Call<JsonObject> setLedState(@Body JsonObject body);

    @GET("/api/profile")
    Call<JsonObject> getProfile();

    @POST("/api/profile")
    Call<JsonObject> createProfile(@Body JsonObject body);

    @DELETE("/api/profile")
    Call<JsonObject> deleteProfile(@Body JsonObject body);
}
