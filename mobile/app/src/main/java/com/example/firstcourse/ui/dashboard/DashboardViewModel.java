package com.example.firstcourse.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.firstcourse.data.model.DeviceStatus;
import com.example.firstcourse.data.repository.DeviceRepository;

/**
 * ViewModel for the Dashboard screen (MainActivity).
 * The ViewModel's role is to provide data to the UI and survive configuration changes (like screen rotations).
 * It communicates with the Repository to get the data, and the UI observes LiveData objects
 * exposed by the ViewModel to update itself.
 * This separation ensures that the Activity/Fragment is only responsible for drawing the UI and handling user input.
 */
public class DashboardViewModel extends ViewModel {

    private final DeviceRepository repository;
    private LiveData<DeviceStatus> deviceStatusLiveData;

    /**
     * The constructor for the ViewModel. It's best practice to not pass any context to ViewModels.
     * We get the repository instance here.
     */
    public DashboardViewModel() {
        this.repository = DeviceRepository.getInstance();
        this.deviceStatusLiveData = repository.getDeviceStatus();
    }

    /**
     * Exposes the LiveData object for the device status, so the UI can observe it.
     * @return A LiveData object containing the latest DeviceStatus.
     */
    public LiveData<DeviceStatus> getDeviceStatus() {
        return deviceStatusLiveData;
    }
}
