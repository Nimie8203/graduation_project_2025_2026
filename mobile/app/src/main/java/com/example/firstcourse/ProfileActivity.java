package com.example.firstcourse;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstcourse.data.model.ApiResult;
import com.example.firstcourse.data.model.IrrigationProfile;
import com.example.firstcourse.ui.profiles.ProfileAdapter;
import com.example.firstcourse.ui.profiles.ProfileViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ProfileViewModel viewModel;
    private ProfileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        // Init ViewModel and Adapter
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        adapter = new ProfileAdapter();

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_profiles);
        recyclerView.setAdapter(adapter);

        // Observe profile list changes
        viewModel.getProfilesLiveData().observe(this, result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                adapter.setProfiles(result.getData());
                if (result.getData().isEmpty() && result.getMessage() != null) {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (result != null && result.getMessage() != null) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup FAB to show create profile dialog
        FloatingActionButton fab = findViewById(R.id.fab_add_profile);
        fab.setOnClickListener(view -> showCreateProfileDialog());
    }

    private void showCreateProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_profile, null);

        final EditText profileName = dialogView.findViewById(R.id.edit_text_profile_name);
        final EditText plantName = dialogView.findViewById(R.id.edit_text_plant_name);
        final EditText timesPerDay = dialogView.findViewById(R.id.edit_text_times_per_day);
        final EditText timesOfDay = dialogView.findViewById(R.id.edit_text_times_of_day);
        final EditText waterAmount = dialogView.findViewById(R.id.edit_text_water_amount);

        builder.setView(dialogView)
                .setPositiveButton("Create", (dialog, id) -> {
                    // Create profile from dialog input
                    try {
                        String pName = profileName.getText().toString().trim();
                        String name = plantName.getText().toString().trim();
                        int timesDay = Integer.parseInt(timesPerDay.getText().toString());
                        String[] timesOfDayStr = timesOfDay.getText().toString().split(",");
                        List<Integer> times = new ArrayList<>();
                        for (String t : timesOfDayStr) {
                            times.add(parseTimeToMinutes(t.trim()));
                        }
                        int amount = Integer.parseInt(waterAmount.getText().toString());

                        if (TextUtils.isEmpty(pName)) {
                            Toast.makeText(this, "Profile name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(this, "Plant name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (times.size() != timesDay) {
                            Toast.makeText(this, "Times count must match times per day", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        IrrigationProfile newProfile = new IrrigationProfile(pName, name, timesDay, times, amount);
                        createProfile(newProfile);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid input. Please check the formats.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    private void createProfile(IrrigationProfile profile) {
        viewModel.createProfile(profile).observe(this, result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                IrrigationProfile createdProfile = result.getData();
                // In a real app, you would refresh the list from the server.
                // Here, we just show a toast for confirmation.
                String successText = String.format(Locale.getDefault(),
                    "Profile %s saved: %d times/day, times %s, water %d ml, plant %s.",
                    createdProfile.getProfileName(),
                        createdProfile.getTimesPerDay(),
                        createdProfile.getTimesOfDay().toString(),
                    createdProfile.getWaterAmount(),
                        createdProfile.getPlantName());

                Toast.makeText(this, successText, Toast.LENGTH_LONG).show();

                viewModel.refreshProfiles();
            } else {
                String message = result != null ? result.getMessage() : "Profile could not be created.";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int parseTimeToMinutes(String token) {
        if (TextUtils.isEmpty(token)) {
            throw new NumberFormatException("Empty time");
        }

        if (token.contains(":")) {
            String[] parts = token.split(":");
            if (parts.length != 2) {
                throw new NumberFormatException("Invalid time");
            }
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new NumberFormatException("Invalid hh:mm");
            }
            return hour * 60 + minute;
        }

        int value = Integer.parseInt(token);
        if (value >= 0 && value <= 1439) {
            return value;
        }

        if (value >= 0 && value <= 2359) {
            int hour = value / 100;
            int minute = value % 100;
            if (hour < 24 && minute < 60) {
                return hour * 60 + minute;
            }
        }

        throw new NumberFormatException("Invalid time range");
    }
}
