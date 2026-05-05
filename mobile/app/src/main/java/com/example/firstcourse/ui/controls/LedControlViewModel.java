package com.example.firstcourse.ui.controls;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.firstcourse.data.model.ApiResult;
import com.example.firstcourse.data.repository.DeviceRepository;

public class LedControlViewModel extends ViewModel {

    private final DeviceRepository repository;

    public LedControlViewModel() {
        this.repository = DeviceRepository.getInstance();
    }

    public LiveData<ApiResult<Boolean>> setLedStatus(boolean turnOn) {
        return repository.setLedStatus(turnOn);
    }

    public LiveData<ApiResult<Boolean>> getLedStatus() {
        return repository.getLedStatus();
    }
}