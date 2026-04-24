package com.example.firstcourse.ui.profiles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firstcourse.data.model.ApiResult;
import com.example.firstcourse.data.model.IrrigationProfile;
import com.example.firstcourse.data.repository.DeviceRepository;

import java.util.List;

/**
 * ViewModel for the Profile screen.
 * It provides profile data to the UI and survives configuration changes.
 */
public class ProfileViewModel extends ViewModel {

    private final DeviceRepository repository;
    private final MediatorLiveData<ApiResult<List<IrrigationProfile>>> profilesLiveData = new MediatorLiveData<>();
    private LiveData<ApiResult<List<IrrigationProfile>>> currentSource;

    public ProfileViewModel() {
        this.repository = DeviceRepository.getInstance();
        refreshProfiles();
    }

    /**
     * Exposes the LiveData for the list of profiles so the UI can observe it.
     * @return LiveData containing the list of profiles.
     */
    public LiveData<ApiResult<List<IrrigationProfile>>> getProfilesLiveData() {
        return profilesLiveData;
    }

    /**
     * Requests a fresh profile list from the repository.
     * @return LiveData containing the latest list of profiles.
     */
    public LiveData<ApiResult<List<IrrigationProfile>>> refreshProfiles() {
        if (currentSource != null) {
            profilesLiveData.removeSource(currentSource);
        }
        currentSource = repository.getIrrigationProfiles();
        profilesLiveData.addSource(currentSource, profilesLiveData::setValue);
        return profilesLiveData;
    }

    /**
     * Initiates the creation of a new profile.
     * @param profile The new profile to be created.
     * @return LiveData containing the server's response after creation.
     */
    public LiveData<ApiResult<IrrigationProfile>> createProfile(IrrigationProfile profile) {
        return repository.createIrrigationProfile(profile);
    }
}
