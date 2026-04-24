package com.example.firstcourse;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.firstcourse.data.model.ApiResult;
import com.example.firstcourse.ui.controls.ControlsViewModel;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

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


        // Setup Sensor Control Cards (example setup, you would fetch the real state from the backend)
        setupSensorControl(R.id.control_humidity, "Humidity Sensor", "Monitor air humidity", R.drawable.ic_water_drop, true);
        setupSensorControl(R.id.control_temperature, "Temperature Sensor", "Monitor ambient temperature", R.drawable.ic_thermostat, true);
        setupSensorControl(R.id.control_soil_moisture, "Soil Moisture Sensor", "Monitor soil water content", R.drawable.ic_spa, true);
        setupSensorControl(R.id.control_light, "Light Sensor", "Monitor light intensity", R.drawable.ic_wb_sunny, true);
        setupSensorControl(R.id.control_air_quality, "Air Quality Sensor", "Monitor air quality index", R.drawable.ic_air, true);

        // Setup Threshold Sliders (example setup, you would fetch real values from the backend)
        setupThresholdSlider(R.id.slider_irrigation, "Irrigation Threshold", "Alert when soil moisture drops below this level.", 30, "%");
        setupThresholdSlider(R.id.slider_temperature, "Temperature Alert", "Alert when temperature exceeds this value.", 28, "°C");
        setupThresholdSlider(R.id.slider_humidity, "Humidity Alert", "Alert when humidity exceeds this level.", 80, "%");
    }

    private void setupSensorControl(int cardId, String title, String description, int iconResId, boolean isChecked) {
        View cardView = findViewById(cardId);
        TextView titleView = cardView.findViewById(R.id.sensor_title);
        TextView descriptionView = cardView.findViewById(R.id.sensor_description);
        ImageView iconView = cardView.findViewById(R.id.sensor_icon);
        SwitchMaterial switchView = cardView.findViewById(R.id.sensor_switch);

        titleView.setText(title);
        descriptionView.setText(description);
        iconView.setImageDrawable(ContextCompat.getDrawable(this, iconResId));
        switchView.setChecked(isChecked);
    }

    private void setupThresholdSlider(int cardId, String title, String description, int value, String unit) {
        View cardView = findViewById(cardId);
        TextView titleView = cardView.findViewById(R.id.slider_title);
        TextView descriptionView = cardView.findViewById(R.id.slider_description);
        TextView valueView = cardView.findViewById(R.id.slider_value);
        Slider slider = cardView.findViewById(R.id.slider);

        titleView.setText(title);
        descriptionView.setText(description);
        valueView.setText(String.format("%d%s", value, unit));
        slider.setValue(value);

        slider.addOnChangeListener((slider1, val, fromUser) -> {
            valueView.setText(String.format("%.0f%s", val, unit));
        });
    }
}
