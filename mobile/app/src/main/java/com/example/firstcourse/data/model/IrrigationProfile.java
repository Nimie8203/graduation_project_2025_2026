package com.example.firstcourse.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a mobile-side irrigation profile.
 * Mobile-only fields: `profileName` and `plantName`.
 * Thresholds are intended to be sent to the embedded device later
 * (moisture, temperature, humidity, light). For now they are stored
 * on the mobile side inside this object.
 */
public class IrrigationProfile {

    @SerializedName("profile_name")
    private String profileName;

    @SerializedName("plant_name")
    private String plantName;

    // Thresholds - will be sent to embedded later when supported
    @SerializedName("moisture_threshold")
    private int moistureThreshold; // 0-100

    @SerializedName("temp_threshold")
    private int tempThreshold; // -100 .. +100

    @SerializedName("humidity_threshold")
    private int humidityThreshold; // 0-100

    @SerializedName("light_threshold")
    private int lightThreshold; // 0-100

    public IrrigationProfile(String profileName, String plantName,
                             int moistureThreshold, int tempThreshold,
                             int humidityThreshold, int lightThreshold) {
        this.profileName = profileName;
        this.plantName = plantName;
        this.moistureThreshold = moistureThreshold;
        this.tempThreshold = tempThreshold;
        this.humidityThreshold = humidityThreshold;
        this.lightThreshold = lightThreshold;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public int getMoistureThreshold() {
        return moistureThreshold;
    }

    public void setMoistureThreshold(int moistureThreshold) {
        this.moistureThreshold = moistureThreshold;
    }

    public int getTempThreshold() {
        return tempThreshold;
    }

    public void setTempThreshold(int tempThreshold) {
        this.tempThreshold = tempThreshold;
    }

    public int getHumidityThreshold() {
        return humidityThreshold;
    }

    public void setHumidityThreshold(int humidityThreshold) {
        this.humidityThreshold = humidityThreshold;
    }

    public int getLightThreshold() {
        return lightThreshold;
    }

    public void setLightThreshold(int lightThreshold) {
        this.lightThreshold = lightThreshold;
    }
}
