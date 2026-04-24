package com.example.firstcourse.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * This class provides a singleton instance of the Retrofit client.
 * Updated to point to ESP32 SoftAP default IP address.
 */
public class RetrofitClient {

    // Default IP address for ESP32 in SoftAP mode is 192.168.4.1
    private static final String BASE_URL = "http://192.168.4.1/";

    private static Retrofit retrofit = null;

    /**
     * Returns a singleton instance of the ApiService.
     * Added ScalarsConverterFactory to handle plain text responses like "OK" from ESP32.
     */
    public static synchronized ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create()) // For plain text responses
                    .addConverterFactory(GsonConverterFactory.create()) // For JSON parsing
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
