package com.example.firstcourse.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This class provides a singleton instance of the Retrofit client.
 * Using a singleton pattern is crucial for performance as it avoids creating a new client
 * for every network request. It centralizes the configuration of the network layer,
 * making it easier to manage and modify.
 */
public class RetrofitClient {

    // IMPORTANT: Replace this with your actual backend URL.
    private static final String BASE_URL = "http://192.168.1.100:5000"; // Example: "http://192.168.1.100:5000"

    private static Retrofit retrofit = null;

    /**
     * Returns a singleton instance of the ApiService.
     * This method is synchronized to ensure thread safety during the creation of the Retrofit instance.
     * @return The ApiService instance.
     */
    public static synchronized ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Gson for JSON parsing
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
