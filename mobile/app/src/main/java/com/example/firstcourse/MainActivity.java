package com.example.firstcourse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.firstcourse.ui.dashboard.DashboardViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DashboardViewModel dashboardViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Setup edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Observe LiveData from ViewModel
        dashboardViewModel.getDeviceStatus().observe(this, deviceStatus -> {
            if (deviceStatus != null) {
                updateDashboard(deviceStatus);
            } else {
                // Show an error message to the user
                Snackbar.make(findViewById(R.id.main), "Failed to load device status.", Snackbar.LENGTH_LONG).show();
            }
        });


        // Setup Chart Cards
        setupChartCard(R.id.chart_temperature, "Temperature Trend");
        setupChartCard(R.id.chart_humidity, "Humidity Levels");

        // --- Navigation Buttons --- //

        // Setup FAB to open Controls screen
        FloatingActionButton fab = findViewById(R.id.fab_controls);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ControlsActivity.class);
            startActivity(intent);
        });

        // Setup Button to open LED Control screen
        Button ledControlButton = findViewById(R.id.button_open_led_control);
        ledControlButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LedControlActivity.class);
            startActivity(intent);
        });

        // Setup Button to open Profiles screen
        Button profilesButton = findViewById(R.id.button_open_profiles);
        profilesButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void updateDashboard(com.example.firstcourse.data.model.DeviceStatus status) {
        setupSensorCard(R.id.card_humidity, "Humidity", String.format(Locale.getDefault(), "%.1f%%", status.getHumidity()), R.drawable.ic_water_drop);
        setupSensorCard(R.id.card_temperature, "Temperature", String.format(Locale.getDefault(), "%.1f°C", status.getTemperature()), R.drawable.ic_thermostat);
        setupSensorCard(R.id.card_soil_moisture, "Soil Moisture", String.format(Locale.getDefault(), "%.1f%%", status.getSoilMoisture()), R.drawable.ic_spa);
        setupSensorCard(R.id.card_battery, "Battery Level", String.format(Locale.getDefault(), "%d%%", status.getBatteryPercentage()), R.drawable.ic_battery_full);
        setupSensorCard(R.id.card_light, "Light Intensity", String.format(Locale.getDefault(), "%d%%", status.getLightIntensity()), R.drawable.ic_wb_sunny);
        
        if (status.getSystemAlerts() != null && !status.getSystemAlerts().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Alert: " + status.getSystemAlerts().get(0), Snackbar.LENGTH_LONG).show();
        }
    }


    private void setupSensorCard(int cardId, String title, String value, int iconResId) {
        View cardView = findViewById(cardId);
        TextView titleView = cardView.findViewById(R.id.sensor_title);
        TextView valueView = cardView.findViewById(R.id.sensor_value);
        ImageView iconView = cardView.findViewById(R.id.sensor_icon);

        if (titleView != null) titleView.setText(title);
        if (valueView != null) valueView.setText(value);
        if (iconView != null) iconView.setImageDrawable(ContextCompat.getDrawable(this, iconResId));
    }

    private void setupChartCard(int cardId, String title) {
        View cardView = findViewById(cardId);
        TextView titleView = cardView.findViewById(R.id.chart_title);
        if (titleView != null) {
            titleView.setText(title);
        }
    }
}
