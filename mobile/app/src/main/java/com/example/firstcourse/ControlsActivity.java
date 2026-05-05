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

        Button pump1StartButton = findViewById(R.id.button_pump_1_start);
        Button pump1StopButton = findViewById(R.id.button_pump_1_stop);
        Button pump2StartButton = findViewById(R.id.button_pump_2_start);
        Button pump2StopButton = findViewById(R.id.button_pump_2_stop);
        Button irrigateNowButton = findViewById(R.id.button_irrigate_now);

        pump1StartButton.setOnClickListener(v -> runPumpAction(pump1StartButton, "Pump 1...", "Start Irrigation", () -> controlsViewModel.startPump1()));
        pump1StopButton.setOnClickListener(v -> runPumpAction(pump1StopButton, "Stopping...", "Stop Irrigation", () -> controlsViewModel.stopPump1()));
        pump2StartButton.setOnClickListener(v -> runPumpAction(pump2StartButton, "Pump 2...", "Start Irrigation", () -> controlsViewModel.startPump2()));
        pump2StopButton.setOnClickListener(v -> runPumpAction(pump2StopButton, "Stopping...", "Stop Irrigation", () -> controlsViewModel.stopPump2()));

        // Combined irrigation action
        irrigateNowButton.setOnClickListener(v -> {
            // Disable the button to prevent multiple clicks
            irrigateNowButton.setEnabled(false);
            irrigateNowButton.setText("Irrigating Both...");

            controlsViewModel.irrigateNow().observe(this, result -> {
                // Re-enable the button
                irrigateNowButton.setEnabled(true);
                irrigateNowButton.setText("Irrigate Both Pumps");

                if (result != null && result.isSuccess() && result.getData() != null) {
                    Snackbar.make(findViewById(R.id.controls_root), result.getData().getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    String message = result != null ? result.getMessage() : "Irrigation request failed.";
                    Snackbar.make(findViewById(R.id.controls_root), message, Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    private void runPumpAction(Button button, String runningText, String idleText, PumpAction action) {
        button.setEnabled(false);
        button.setText(runningText);

        action.run().observe(this, result -> {
            button.setEnabled(true);
            button.setText(idleText);

            if (result != null && result.isSuccess() && result.getData() != null) {
                Snackbar.make(findViewById(R.id.controls_root), result.getData().getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                String message = result != null ? result.getMessage() : "Pump request failed.";
                Snackbar.make(findViewById(R.id.controls_root), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private interface PumpAction {
        androidx.lifecycle.LiveData<com.example.firstcourse.data.model.ApiResult<com.example.firstcourse.data.model.IrrigationResponse>> run();
    }
}
