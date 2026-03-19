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
        viewModel.getProfilesLiveData().observe(this, profiles -> {
            if (profiles != null) {
                adapter.setProfiles(profiles);
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

        final EditText plantName = dialogView.findViewById(R.id.edit_text_plant_name);
        final EditText timesPerDay = dialogView.findViewById(R.id.edit_text_times_per_day);
        final EditText timesOfDay = dialogView.findViewById(R.id.edit_text_times_of_day);
        final EditText waterAmount = dialogView.findViewById(R.id.edit_text_water_amount);

        builder.setView(dialogView)
                .setPositiveButton("Create", (dialog, id) -> {
                    // Create profile from dialog input
                    try {
                        String name = plantName.getText().toString();
                        int timesDay = Integer.parseInt(timesPerDay.getText().toString());
                        String[] timesOfDayStr = timesOfDay.getText().toString().split(",");
                        List<Integer> times = new ArrayList<>();
                        for (String t : timesOfDayStr) {
                            times.add(Integer.parseInt(t.trim()));
                        }
                        int amount = (int) (Double.parseDouble(waterAmount.getText().toString()));

                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(this, "Plant name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        IrrigationProfile newProfile = new IrrigationProfile(name, timesDay, times, amount);
                        createProfile(newProfile);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid input. Please check the formats.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    private void createProfile(IrrigationProfile profile) {
        viewModel.createProfile(profile).observe(this, createdProfile -> {
            if (createdProfile != null) {
                // In a real app, you would refresh the list from the server.
                // Here, we just show a toast for confirmation.
                String result = String.format(Locale.getDefault(),
                        "Profile was init with: %d times per day, at %s times, a %.2f water amount, with %s name.",
                        createdProfile.getTimesPerDay(),
                        createdProfile.getTimesOfDay().toString(),
                        (double) createdProfile.getWaterAmount(),
                        createdProfile.getPlantName());

                Toast.makeText(this, result, Toast.LENGTH_LONG).show();

                // To refresh the list, you might re-fetch or add to the current list
                viewModel.getProfilesLiveData().observe(this, profiles -> adapter.setProfiles(profiles));

            } else {
                Toast.makeText(this, "Failed to create profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
