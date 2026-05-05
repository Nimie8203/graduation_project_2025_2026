package com.example.firstcourse.ui.controls;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.firstcourse.data.model.ApiResult;
import com.example.firstcourse.data.model.IrrigationResponse;
import com.example.firstcourse.data.repository.DeviceRepository;

/**
 * ViewModel for the Controls screen (ControlsActivity).
 * This ViewModel handles the business logic for actions like manual irrigation.
 * It communicates with the repository to perform actions and provides the results
 * back to the UI via LiveData.
 */
public class ControlsViewModel extends ViewModel {

    private final DeviceRepository repository;

    public ControlsViewModel() {
        this.repository = DeviceRepository.getInstance();
    }

    /**
     * Triggers the manual irrigation process.
     * It calls the repository method and returns a LiveData object that the UI can observe
     * to know when the operation is complete and whether it was successful.
     * @return A LiveData object containing the IrrigationResponse from the server.
     */
    public LiveData<ApiResult<IrrigationResponse>> irrigateNow() {
        return repository.irrigateNow();
    }

    public LiveData<ApiResult<IrrigationResponse>> startPump1() {
        return repository.startPump1();
    }

    public LiveData<ApiResult<IrrigationResponse>> stopPump1() {
        return repository.stopPump1();
    }

    public LiveData<ApiResult<IrrigationResponse>> startPump2() {
        return repository.startPump2();
    }

    public LiveData<ApiResult<IrrigationResponse>> stopPump2() {
        return repository.stopPump2();
    }

    // You could add methods here to handle profile creation/updating in the future.
}
