package com.example.firstcourse.data.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * This interface defines the REST API endpoints for our application.
 * Retrofit uses this interface to generate the HTTP client.
 * Each method represents one API call, annotated with HTTP methods like @GET or @POST.
 * The return types are wrapped in a Retrofit Call object, which allows for asynchronous
 * or synchronous execution.
 */
public interface ApiService {
    @GET("/api/cmd")
    Call<String> sendCommand(@Query("val") int command);
}
