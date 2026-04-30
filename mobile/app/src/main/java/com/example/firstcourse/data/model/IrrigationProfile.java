package com.example.firstcourse.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents an irrigation profile for a specific plant.
 * This POJO is used by Gson to parse and create profile objects from JSON.
 * It's also used to send a new profile to the backend via a POST request.
 */
public class IrrigationProfile {

    @SerializedName("profile_name")
    private String profileName;

    @SerializedName("plant_name")
    private String plantName;

    @SerializedName("irrig_times_per_day")
    private int timesPerDay;

    @SerializedName("times_of_day")
    private List<Integer> timesOfDay;

    @SerializedName("water_amount_per_irrig")
    private int waterAmount;

    // Constructor, getters, and setters

    public IrrigationProfile(String profileName, String plantName, int timesPerDay, List<Integer> timesOfDay, int waterAmount) {
        this.profileName = profileName;
        this.plantName = plantName;
        this.timesPerDay = timesPerDay;
        this.timesOfDay = timesOfDay;
        this.waterAmount = waterAmount;
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

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }

    public List<Integer> getTimesOfDay() {
        return timesOfDay;
    }

    public void setTimesOfDay(List<Integer> timesOfDay) {
        this.timesOfDay = timesOfDay;
    }

    public int getWaterAmount() {
        return waterAmount;
    }

    public void setWaterAmount(int waterAmount) {
        this.waterAmount = waterAmount;
    }
}
