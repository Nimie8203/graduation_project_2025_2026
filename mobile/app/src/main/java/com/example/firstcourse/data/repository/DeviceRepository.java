package com.example.firstcourse.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.firstcourse.data.mock.MockData;
import com.example.firstcourse.data.model.DeviceStatus;
import com.example.firstcourse.data.model.IrrigationProfile;
import com.example.firstcourse.data.model.IrrigationResponse;
import com.example.firstcourse.data.remote.ApiService;
import com.example.firstcourse.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceRepository {

    private static final boolean USE_MOCK_DATA = true;
    private static DeviceRepository instance;
    private final ApiService apiService;

    private DeviceRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public static synchronized DeviceRepository getInstance() {
        if (instance == null) {
            instance = new DeviceRepository();
        }
        return instance;
    }

    public LiveData<DeviceStatus> getDeviceStatus() {
        MutableLiveData<DeviceStatus> data = new MutableLiveData<>();
        if (USE_MOCK_DATA) {
            data.setValue(MockData.getMockDeviceStatus());
        } else {
            apiService.getDeviceStatus().enqueue(new Callback<DeviceStatus>() {
                @Override
                public void onResponse(Call<DeviceStatus> call, Response<DeviceStatus> response) {
                    data.setValue(response.body());
                }

                @Override
                public void onFailure(Call<DeviceStatus> call, Throwable t) {
                    data.setValue(null);
                }
            });
        }
        return data;
    }

    public LiveData<IrrigationResponse> irrigateNow() {
        MutableLiveData<IrrigationResponse> data = new MutableLiveData<>();
        if (USE_MOCK_DATA) {
            data.setValue(MockData.getMockIrrigationResponse());
        } else {
            apiService.irrigateNow().enqueue(new Callback<IrrigationResponse>() {
                @Override
                public void onResponse(Call<IrrigationResponse> call, Response<IrrigationResponse> response) {
                    data.setValue(response.body());
                }

                @Override
                public void onFailure(Call<IrrigationResponse> call, Throwable t) {
                    data.setValue(null);
                }
            });
        }
        return data;
    }

    /**
     * Fetches the list of irrigation profiles.
     * @return A LiveData object containing the list of profiles.
     */
    public LiveData<List<IrrigationProfile>> getIrrigationProfiles() {
        MutableLiveData<List<IrrigationProfile>> data = new MutableLiveData<>();
        if (USE_MOCK_DATA) {
            data.setValue(MockData.getMockIrrigationProfiles());
        } else {
            apiService.getIrrigationProfiles().enqueue(new Callback<List<IrrigationProfile>>() {
                @Override
                public void onResponse(Call<List<IrrigationProfile>> call, Response<List<IrrigationProfile>> response) {
                    data.setValue(response.body());
                }

                @Override
                public void onFailure(Call<List<IrrigationProfile>> call, Throwable t) {
                    data.setValue(null);
                }
            });
        }
        return data;
    }

    /**
     * Creates a new irrigation profile.
     * @param profile The profile to create.
     * @return A LiveData object containing the created profile from the server.
     */
    public LiveData<IrrigationProfile> createIrrigationProfile(IrrigationProfile profile) {
        MutableLiveData<IrrigationProfile> data = new MutableLiveData<>();
        if (USE_MOCK_DATA) {
            // In mock mode, just return the profile we "created" with a small delay.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            data.setValue(profile);
        } else {
            apiService.createIrrigationProfile(profile).enqueue(new Callback<IrrigationProfile>() {
                @Override
                public void onResponse(Call<IrrigationProfile> call, Response<IrrigationProfile> response) {
                    data.setValue(response.body());
                }

                @Override
                public void onFailure(Call<IrrigationProfile> call, Throwable t) {
                    data.setValue(null);
                }
            });
        }
        return data;
    }
}
