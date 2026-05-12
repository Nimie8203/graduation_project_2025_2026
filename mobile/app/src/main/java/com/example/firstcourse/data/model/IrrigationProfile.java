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

    // Band thresholds - sent directly to embedded device
    @SerializedName("moist_upper")
    private int moistUpper; // 0-100

    @SerializedName("moist_lower")
    private int moistLower; // 0-100

    @SerializedName("temp_upper")
    private int tempUpper; // -100 .. +100

    @SerializedName("temp_lower")
    private int tempLower; // -100 .. +100

    @SerializedName("humidity_upper")
    private int humUpper; // 0-100

    @SerializedName("humidity_lower")
    private int humLower; // 0-100

    @SerializedName("light_threshold")
    private int lightThreshold; // 0-100

    public IrrigationProfile(String profileName, String plantName,
                             int moistUpper, int moistLower,
                             int tempUpper, int tempLower,
                             int humUpper, int humLower,
                             int lightThreshold) {
        this.profileName = profileName;
        this.plantName = plantName;
        this.moistUpper = moistUpper;
        this.moistLower = moistLower;
        this.tempUpper = tempUpper;
        this.tempLower = tempLower;
        this.humUpper = humUpper;
        this.humLower = humLower;
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

    public int getMoistUpper() {
        return moistUpper;
    }

    public void setMoistUpper(int moistUpper) {
        this.moistUpper = moistUpper;
    }

    public int getMoistLower() {
        return moistLower;
    }

    public void setMoistLower(int moistLower) {
        this.moistLower = moistLower;
    }

    public int getTempUpper() {
        return tempUpper;
    }

    public void setTempUpper(int tempUpper) {
        this.tempUpper = tempUpper;
    }

    public int getTempLower() {
        return tempLower;
    }

    public void setTempLower(int tempLower) {
        this.tempLower = tempLower;
    }

    public int getHumUpper() {
        return humUpper;
    }

    public void setHumUpper(int humUpper) {
        this.humUpper = humUpper;
    }

    public int getHumLower() {
        return humLower;
    }

    public void setHumLower(int humLower) {
        this.humLower = humLower;
    }

    public int getLightThreshold() {
        return lightThreshold;
    }

    public void setLightThreshold(int lightThreshold) {
        this.lightThreshold = lightThreshold;
    }
}
