package com.example.firstcourse.ui.profiles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstcourse.R;
import com.example.firstcourse.data.model.IrrigationProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter to display a list of IrrigationProfile objects in a RecyclerView.
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private final List<IrrigationProfile> profiles = new ArrayList<>();
    private OnProfileDeleteListener deleteListener;
    private OnProfileActiveListener activeListener;

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        IrrigationProfile profile = profiles.get(position);
        holder.bind(profile);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * Updates the list of profiles in the adapter.
     * @param newProfiles The new list of profiles.
     */
    public void setProfiles(List<IrrigationProfile> newProfiles) {
        this.profiles.clear();
        if (newProfiles != null) {
            this.profiles.addAll(newProfiles);
        }
        notifyDataSetChanged(); // In a real app, use DiffUtil for better performance
    }

    /**
     * Sets the listener for profile deletion events.
     * @param listener The listener to be called when a profile is deleted.
     */
    public void setDeleteListener(OnProfileDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setActiveListener(OnProfileActiveListener listener) {
        this.activeListener = listener;
    }

    /**
     * ViewHolder for a single profile item.
     */
    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final TextView profileName, plantName, timesPerDay, thresholdSummary, timesOfDay;
        private final Button deleteButton, activeButton;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profile_name);
            plantName = itemView.findViewById(R.id.profile_plant_name);
            timesPerDay = itemView.findViewById(R.id.profile_times_per_day);
            thresholdSummary = itemView.findViewById(R.id.profile_threshold_summary);
            timesOfDay = itemView.findViewById(R.id.profile_times_of_day);
            deleteButton = itemView.findViewById(R.id.btn_delete_profile);
            activeButton = itemView.findViewById(R.id.btn_active_profile);
        }

        public void bind(IrrigationProfile profile) {
            profileName.setText(profile.getProfileName());
            plantName.setText(profile.getPlantName());
            // Show thresholds instead of irrigation schedule
            timesPerDay.setText(String.format(Locale.getDefault(), "M:%d T:%d H:%d L:%d",
                    profile.getMoistureThreshold(), profile.getTempThreshold(),
                    profile.getHumidityThreshold(), profile.getLightThreshold()));

            thresholdSummary.setText(String.format(Locale.getDefault(),
                    "Moisture %d%%  Temp %d°C  Humidity %d%%  Light %d",
                    profile.getMoistureThreshold(), profile.getTempThreshold(),
                    profile.getHumidityThreshold(), profile.getLightThreshold()));
            timesOfDay.setText("Thresholds ready");

            // Set delete button listener
            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onProfileDelete(profile.getProfileName());
                }
            });

            activeButton.setOnClickListener(v -> {
                if (activeListener != null) {
                    activeListener.onProfileActive(profile);
                }
            });
        }
    }

    /**
     * Interface for listening to profile deletion events.
     */
    public interface OnProfileDeleteListener {
        void onProfileDelete(String profileName);
    }

    public interface OnProfileActiveListener {
        void onProfileActive(IrrigationProfile profile);
    }
}