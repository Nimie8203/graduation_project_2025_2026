package com.example.firstcourse;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.firstcourse.ui.controls.LedControlViewModel;

/**
 * An activity to control ESP32 LED through MVVM and repository network layer.
 */
public class LedControlActivity extends AppCompatActivity {

    // UI Elements
    private Button buttonLedOn, buttonLedOff, buttonLedStatus;
    private TextView textViewResult;
    private LedControlViewModel ledControlViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        buttonLedOn = findViewById(R.id.button_led_on);
        buttonLedOff = findViewById(R.id.button_led_off);
        buttonLedStatus = findViewById(R.id.button_led_status);
        textViewResult = findViewById(R.id.textview_result);
        ledControlViewModel = new ViewModelProvider(this).get(LedControlViewModel.class);

        buttonLedOn.setOnClickListener(v -> {
            textViewResult.setText("Sending command...");
            ledControlViewModel.setLedStatus(true).observe(this, result -> {
                if (result != null && result.isSuccess()) {
                    textViewResult.setText("LED On - " + result.getMessage());
                } else {
                    textViewResult.setText(result != null ? result.getMessage() : "LED ON command failed.");
                }
            });
        });

        buttonLedOff.setOnClickListener(v -> {
            textViewResult.setText("Sending command...");
            ledControlViewModel.setLedStatus(false).observe(this, result -> {
                if (result != null && result.isSuccess()) {
                    textViewResult.setText("LED Off - " + result.getMessage());
                } else {
                    textViewResult.setText(result != null ? result.getMessage() : "LED OFF command failed.");
                }
            });
        });

        buttonLedStatus.setOnClickListener(v -> {
            textViewResult.setText("Querying LED status...");
            ledControlViewModel.getLedStatus().observe(this, result -> {
                if (result != null && result.isSuccess() && result.getData() != null) {
                    textViewResult.setText(result.getData() ? "LED Status: ON" : "LED Status: OFF");
                } else {
                    textViewResult.setText(result != null ? result.getMessage() : "Failed to get LED status.");
                }
            });
        });
    }
}
