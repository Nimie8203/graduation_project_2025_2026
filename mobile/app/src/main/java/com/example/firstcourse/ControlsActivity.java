package com.example.firstcourse;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.firstcourse.ui.controls.ControlsViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ControlsActivity extends AppCompatActivity {

    private ControlsViewModel controlsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        // Initialize ViewModel
        controlsViewModel = new ViewModelProvider(this).get(ControlsViewModel.class);

        // Setup Manual Irrigation
        Button irrigateNowButton = findViewById(R.id.button_irrigate_now);
        irrigateNowButton.setOnClickListener(v -> {
            // Disable the button to prevent multiple clicks
            irrigateNowButton.setEnabled(false);
            irrigateNowButton.setText("Irrigating...");

            controlsViewModel.irrigateNow().observe(this, result -> {
                // Re-enable the button
                irrigateNowButton.setEnabled(true);
                irrigateNowButton.setText("Irrigate Now");

                if (result != null && result.isSuccess() && result.getData() != null) {
                    Snackbar.make(findViewById(R.id.controls_root), result.getData().getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    String message = result != null ? result.getMessage() : "Irrigation request failed.";
                    Snackbar.make(findViewById(R.id.controls_root), message, Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }
}
