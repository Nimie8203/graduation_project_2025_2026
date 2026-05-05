package com.example.firstcourse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.firstcourse.data.model.DeviceStatus;
import com.example.firstcourse.ui.dashboard.AnimatedDashboardView;
import com.example.firstcourse.data.model.ApiResult;
import com.example.firstcourse.ui.dashboard.DashboardViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 3000L;
    private static final boolean USE_DYNAMIC_MOCK_DATA = false;

    private DashboardViewModel dashboardViewModel;
    private AnimatedDashboardView animatedDashboardView;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            // Use real backend refresh. Mock mode is disabled in this build.
            dashboardViewModel.refreshDeviceStatus();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animatedDashboardView = findViewById(R.id.animated_dashboard_view);

        // Initialize ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Setup edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Observe LiveData only when backend mode is active.
        if (!USE_DYNAMIC_MOCK_DATA) {
            dashboardViewModel.getDeviceStatus().observe(this, result -> {
                if (result != null && result.isSuccess() && result.getData() != null) {
                    updateDashboard(result.getData());
                } else {
                    String message = result != null ? result.getMessage() : "Failed to load device status.";
                    Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show();
                }
            });
        }

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

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void updateDashboard(DeviceStatus status) {
        if (animatedDashboardView != null) {
            animatedDashboardView.setStatus(status);
        }

        setupSensorCard(R.id.card_humidity, "Humidity", String.format(Locale.getDefault(), "%.1f%%", status.getHumidity()), R.drawable.ic_water_drop);
        setupSensorCard(R.id.card_temperature, "Temperature", String.format(Locale.getDefault(), "%.1f°C", status.getTemperature()), R.drawable.ic_thermostat);
        setupSensorCard(R.id.card_light, "Light", String.format(Locale.getDefault(), "%d", status.getLightIntensity()), R.drawable.ic_wb_sunny);
        setupSensorCard(R.id.card_flow_1, "Flow 1", String.format(Locale.getDefault(), "%.1f", status.getFlowSensor1()), R.drawable.ic_water_drop);
        setupSensorCard(R.id.card_flow_2, "Flow 2", String.format(Locale.getDefault(), "%.1f", status.getFlowSensor2()), R.drawable.ic_water_drop);
        setupSensorCard(R.id.card_moisture_1, "Soil 1", String.format(Locale.getDefault(), "%.1f%%", status.getMoisture1()), R.drawable.ic_spa);
        setupSensorCard(R.id.card_moisture_2, "Soil 2", String.format(Locale.getDefault(), "%.1f%%", status.getMoisture2()), R.drawable.ic_spa);
        setupSensorCard(R.id.card_moisture_3, "Soil 3", String.format(Locale.getDefault(), "%.1f%%", status.getMoisture3()), R.drawable.ic_spa);
        setupSensorCard(R.id.card_moisture_4, "Soil 4", String.format(Locale.getDefault(), "%.1f%%", status.getMoisture4()), R.drawable.ic_spa);
        
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

}
