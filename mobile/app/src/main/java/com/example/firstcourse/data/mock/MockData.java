package com.example.firstcourse.data.mock;

import com.example.firstcourse.data.model.DeviceStatus;
import com.example.firstcourse.data.model.IrrigationProfile;
import com.example.firstcourse.data.model.IrrigationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides mock data for testing purposes.
 */
public class MockData {

    // Static list to hold profiles across mock create/delete operations
    private static final List<IrrigationProfile> mockProfiles = new ArrayList<>();

    static {
        // Initialize with default profiles
        // Use thresholds: moisture(0-100), temp(-100..100), humidity(0-100), light(0-100)
        mockProfiles.add(new IrrigationProfile("Tomato Day Plan", "Tomato", 50, 22, 60, 75));
        mockProfiles.add(new IrrigationProfile("Cucumber Day Plan", "Cucumber", 55, 20, 65, 70));
        mockProfiles.add(new IrrigationProfile("Strawberry Day Plan", "Strawberry", 48, 18, 62, 68));
    }

    public static DeviceStatus getDynamicDeviceStatus(long timeMs) {
        double t = timeMs / 1000.0;

        DeviceStatus status = new DeviceStatus();

        boolean irrigating = ((int) (t / 12.0)) % 2 == 0;

        double temperature = 22.0 + 4.5 * Math.sin(t / 9.0);
        double humidity = 58.0 + 17.0 * Math.sin((t / 11.0) + 1.2);
        int light = (int) Math.round(clamp(50.0 + 50.0 * Math.sin((t / 8.0) - 0.8), 0.0, 100.0));

        double flow1;
        double flow2;
        if (irrigating) {
            flow1 = 1.8 + 2.2 * pulseWave(t, 0.0);
            flow2 = 1.6 + 2.0 * pulseWave(t, 1.7);
        } else {
            flow1 = 0.15 + 0.10 * pulseWave(t, 0.4);
            flow2 = 0.12 + 0.08 * pulseWave(t, 1.1);
        }

        double moistureBase = 45.0 + 12.0 * Math.sin((t / 14.0) + 2.1);
        double moistureTrend = irrigating ? 8.0 : -6.0;

        status.setTemperature(round1(temperature));
        status.setHumidity(round1(clamp(humidity, 20.0, 95.0)));
        status.setLightIntensity(light);
        status.setFlowSensor1(round1(clamp(flow1, 0.0, 8.0)));
        status.setFlowSensor2(round1(clamp(flow2, 0.0, 8.0)));

        status.setMoisture1(round1(clamp(moistureBase + moistureTrend + 4.0 * Math.sin(t / 4.0), 5.0, 100.0)));
        status.setMoisture2(round1(clamp(moistureBase + moistureTrend - 3.0 * Math.sin(t / 5.0), 5.0, 100.0)));
        status.setMoisture3(round1(clamp(moistureBase + moistureTrend + 2.5 * Math.sin((t / 3.5) + 0.8), 5.0, 100.0)));
        status.setMoisture4(round1(clamp(moistureBase + moistureTrend - 3.5 * Math.sin((t / 6.5) + 2.0), 5.0, 100.0)));

        status.setLed(light < 35);
        status.setPump1(irrigating);
        status.setPump2(irrigating);
        status.setSystemAlerts(new ArrayList<>(Arrays.asList(
                "MOCK MODE: Dynamic data active",
                irrigating ? "Irrigation cycle: ON" : "Irrigation cycle: OFF"
        )));

        return status;
    }

    public static DeviceStatus getMockDeviceStatus() {
        DeviceStatus status = new DeviceStatus();
        status.setTemperature(23.8);
        status.setHumidity(68.0);
        status.setLightIntensity(75);
        status.setFlowSensor1(2.4);
        status.setFlowSensor2(2.1);
        status.setMoisture1(55.5);
        status.setMoisture2(57.0);
        status.setMoisture3(53.8);
        status.setMoisture4(56.2);
        status.setSystemAlerts(new ArrayList<>(Arrays.asList("LOW_BATTERY")));
        return status;
    }

    private static double pulseWave(double t, double phase) {
        return 0.5 + 0.5 * Math.sin((t + phase) * 2.1);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static IrrigationResponse getMockIrrigationResponse() {
        IrrigationResponse response = new IrrigationResponse();
        response.setMessage("Mock irrigation started successfully!");
        return response;
    }

    /**
     * Provides the list of IrrigationProfile objects (including dynamically added profiles).
     * @return The current list of irrigation profiles.
     */
    public static List<IrrigationProfile> getMockIrrigationProfiles() {
        return new ArrayList<>(mockProfiles);
    }

    /**
     * Adds a new profile to the mock profiles list.
     * @param profile The profile to add.
     */
    public static void addMockProfile(IrrigationProfile profile) {
        if (profile != null) {
            mockProfiles.add(profile);
        }
    }

    /**
     * Removes a profile by name from the mock profiles list.
     * @param profileName The name of the profile to remove.
     */
    public static void removeMockProfile(String profileName) {
        if (profileName != null) {
            mockProfiles.removeIf(p -> p.getProfileName().equals(profileName));
        }
    }
}
