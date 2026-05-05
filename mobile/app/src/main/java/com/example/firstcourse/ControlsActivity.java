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

        Button pump1Button = findViewById(R.id.button_pump_1);
        Button pump2Button = findViewById(R.id.button_pump_2);
        Button irrigateNowButton = findViewById(R.id.button_irrigate_now);

        pump1Button.setOnClickListener(v -> {
            pump1Button.setEnabled(false);
            pump1Button.setText("Pump 1...");

            controlsViewModel.startPump1().observe(this, result -> {
                pump1Button.setEnabled(true);
                pump1Button.setText("Pump 1");

                if (result != null && result.isSuccess() && result.getData() != null) {
                    Snackbar.make(findViewById(R.id.controls_root), result.getData().getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    String message = result != null ? result.getMessage() : "Pump 1 request failed.";
                    Snackbar.make(findViewById(R.id.controls_root), message, Snackbar.LENGTH_LONG).show();
                }
            });
        });

        pump2Button.setOnClickListener(v -> {
            pump2Button.setEnabled(false);
            pump2Button.setText("Pump 2...");

            controlsViewModel.startPump2().observe(this, result -> {
                pump2Button.setEnabled(true);
                pump2Button.setText("Pump 2");

                if (result != null && result.isSuccess() && result.getData() != null) {
                    Snackbar.make(findViewById(R.id.controls_root), result.getData().getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    String message = result != null ? result.getMessage() : "Pump 2 request failed.";
                    Snackbar.make(findViewById(R.id.controls_root), message, Snackbar.LENGTH_LONG).show();
                }
            });
        });

        // Combined irrigation action
        irrigateNowButton.setOnClickListener(v -> {
            // Disable the button to prevent multiple clicks
            irrigateNowButton.setEnabled(false);
            irrigateNowButton.setText("Irrigating All...");

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
