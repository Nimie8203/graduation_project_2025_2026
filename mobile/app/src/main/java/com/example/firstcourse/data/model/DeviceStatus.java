package com.example.firstcourse.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the latest status of the irrigation device.
 * This is a Plain Old Java Object (POJO) used by Gson to parse the JSON response
 * from the backend API. Using @SerializedName allows us to decouple our Java
 * field names from the JSON key names, providing flexibility.
 */
public class DeviceStatus {

    @SerializedName("soil_moisture")
    private double soilMoisture;

    @SerializedName("temperature")
    private double temperature;

    @SerializedName("humidity")
    private double humidity;

    @SerializedName("battery_percentage")
    private int batteryPercentage;

    @SerializedName("light_intensity")
    private int lightIntensity;

    @SerializedName("system_alerts")
    private List<String> systemAlerts;

    // Getters and setters for all fields

    public double getSoilMoisture() {
        return soilMoisture;
    }

    public void setSoilMoisture(double soilMoisture) {
        this.soilMoisture = soilMoisture;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public int getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(int lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public List<String> getSystemAlerts() {
        return systemAlerts;
    }

    public void setSystemAlerts(List<String> systemAlerts) {
        this.systemAlerts = systemAlerts;
    }
}
