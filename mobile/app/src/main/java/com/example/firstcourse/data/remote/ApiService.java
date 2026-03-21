package com.example.firstcourse.data.remote;

import com.example.firstcourse.data.model.DeviceStatus;
import com.example.firstcourse.data.model.IrrigationProfile;
import com.example.firstcourse.data.model.IrrigationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
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

    /**
     * Fetches the latest device status from the backend.
     * @return A Retrofit Call object that can be executed to get the DeviceStatus.
     */
    @GET("/api/device/status/latest")
    Call<DeviceStatus> getDeviceStatus();

    /**
     * Fetches all irrigation profiles from the backend.
     * @return A Retrofit Call object for a list of IrrigationProfiles.
     */
    @GET("/api/profiles")
    Call<List<IrrigationProfile>> getIrrigationProfiles();

    /**
     * Creates a new irrigation profile on the backend.
     * @param profile The IrrigationProfile object to be created. It will be serialized to JSON.
     * @return A Retrofit Call object for the created IrrigationProfile.
     */
    @POST("/api/profiles")
    Call<IrrigationProfile> createIrrigationProfile(@Body IrrigationProfile profile);

    /**
     * Triggers manual irrigation.
     * @return A Retrofit Call object for the IrrigationResponse.
     */
    @POST("/api/device/irrigate")
    Call<IrrigationResponse> irrigateNow();
}
