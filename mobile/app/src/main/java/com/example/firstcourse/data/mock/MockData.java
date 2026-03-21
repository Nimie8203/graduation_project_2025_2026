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

    public static DeviceStatus getMockDeviceStatus() {
        DeviceStatus status = new DeviceStatus();
        status.setSoilMoisture(55.5);
        status.setTemperature(23.8);
        status.setHumidity(68.0);
        status.setBatteryPercentage(85);
        status.setLightIntensity(75);
        status.setSystemAlerts(new ArrayList<>(Arrays.asList("LOW_BATTERY")));
        return status;
    }

    public static IrrigationResponse getMockIrrigationResponse() {
        IrrigationResponse response = new IrrigationResponse();
        response.setMessage("Mock irrigation started successfully!");
        return response;
    }

    /**
     * Provides a fake list of IrrigationProfile objects.
     * @return A list of sample irrigation profiles.
     */
    public static List<IrrigationProfile> getMockIrrigationProfiles() {
        List<IrrigationProfile> profiles = new ArrayList<>();
        profiles.add(new IrrigationProfile("Tomato", 3, new ArrayList<>(Arrays.asList(800, 1200, 1800)), 2));
        profiles.add(new IrrigationProfile("Cucumber", 4, new ArrayList<>(Arrays.asList(700, 1100, 1500, 1900)), 3));
        profiles.add(new IrrigationProfile("Strawberry", 2, new ArrayList<>(Arrays.asList(900, 1600)), 1));
        return profiles;
    }
}
