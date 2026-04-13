package com.example.firstcourse.ui.profiles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.firstcourse.data.model.IrrigationProfile;
import com.example.firstcourse.data.repository.DeviceRepository;

import java.util.List;

/**
 * ViewModel for the Profile screen.
 * It provides profile data to the UI and survives configuration changes.
 */
public class ProfileViewModel extends ViewModel {

    private final DeviceRepository repository;
    private final LiveData<List<IrrigationProfile>> profilesLiveData;

    public ProfileViewModel() {
        this.repository = DeviceRepository.getInstance();
        this.profilesLiveData = repository.getIrrigationProfiles();
    }

    /**
     * Exposes the LiveData for the list of profiles so the UI can observe it.
     * @return LiveData containing the list of profiles.
     */
    public LiveData<List<IrrigationProfile>> getProfilesLiveData() {
        return profilesLiveData;
    }

    /**
     * Initiates the creation of a new profile.
     * @param profile The new profile to be created.
     * @return LiveData containing the server's response after creation.
     */
    public LiveData<IrrigationProfile> createProfile(IrrigationProfile profile) {
        return repository.createIrrigationProfile(profile);
    }
}
