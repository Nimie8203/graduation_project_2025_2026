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

    /**
     * ViewHolder for a single profile item.
     */
    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final TextView profileName, plantName, timesPerDay, waterAmount, timesOfDay;
        private final Button deleteButton;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profile_name);
            plantName = itemView.findViewById(R.id.profile_plant_name);
            timesPerDay = itemView.findViewById(R.id.profile_times_per_day);
            waterAmount = itemView.findViewById(R.id.profile_water_amount);
            timesOfDay = itemView.findViewById(R.id.profile_times_of_day);
            deleteButton = itemView.findViewById(R.id.btn_delete_profile);
        }

        public void bind(IrrigationProfile profile) {
            profileName.setText(profile.getProfileName());
            plantName.setText(profile.getPlantName());
            timesPerDay.setText(String.format(Locale.getDefault(), "%d times per day", profile.getTimesPerDay()));
            waterAmount.setText(String.format(Locale.getDefault(), "%d ml", profile.getWaterAmount()));

            // Format times of day
            StringBuilder timesString = new StringBuilder("At: ");
            for (int i = 0; i < profile.getTimesOfDay().size(); i++) {
                int minuteOfDay = profile.getTimesOfDay().get(i);
                int hour = minuteOfDay / 60;
                int minute = minuteOfDay % 60;
                timesString.append(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                if (i < profile.getTimesOfDay().size() - 1) {
                    timesString.append(", ");
                }
            }
            timesOfDay.setText(timesString.toString());

            // Set delete button listener
            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onProfileDelete(profile.getProfileName());
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
}