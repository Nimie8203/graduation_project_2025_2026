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
        profiles.add(new IrrigationProfile("Tomato Day Plan", "Tomato", 3, new ArrayList<>(Arrays.asList(480, 720, 1080)), 500));
        profiles.add(new IrrigationProfile("Cucumber Day Plan", "Cucumber", 4, new ArrayList<>(Arrays.asList(420, 660, 900, 1140)), 600));
        profiles.add(new IrrigationProfile("Strawberry Day Plan", "Strawberry", 2, new ArrayList<>(Arrays.asList(540, 960)), 400));
        return profiles;
    }
}
