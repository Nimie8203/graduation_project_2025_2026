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

    @SerializedName("temperature")
    private double temperature;

    @SerializedName("humidity")
    private double humidity;

    @SerializedName("light_intensity")
    private int lightIntensity;

    @SerializedName("flow_sensor_1")
    private double flowSensor1;

    @SerializedName("flow_sensor_2")
    private double flowSensor2;

    @SerializedName("moisture_1")
    private double moisture1;

    @SerializedName("moisture_2")
    private double moisture2;

    @SerializedName("moisture_3")
    private double moisture3;

    @SerializedName("moisture_4")
    private double moisture4;

    @SerializedName("led")
    private boolean led;

    @SerializedName("pump_1")
    private boolean pump1;

    @SerializedName("pump_2")
    private boolean pump2;

    @SerializedName("pump_1_triggered")
    private boolean pump1Triggered;

    @SerializedName("pump_2_triggered")
    private boolean pump2Triggered;

    @SerializedName("tank")
    private boolean tank;

    @SerializedName("pipe")
    private boolean pipe;

    @SerializedName("profile")
    private EmbeddedProfile profile;

    @SerializedName("system_alerts")
    private List<String> systemAlerts;

    // Getters and setters for all fields

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

    public int getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(int lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public double getFlowSensor1() {
        return flowSensor1;
    }

    public void setFlowSensor1(double flowSensor1) {
        this.flowSensor1 = flowSensor1;
    }

    public double getFlowSensor2() {
        return flowSensor2;
    }

    public void setFlowSensor2(double flowSensor2) {
        this.flowSensor2 = flowSensor2;
    }

    public double getMoisture1() {
        return moisture1;
    }

    public void setMoisture1(double moisture1) {
        this.moisture1 = moisture1;
    }

    public double getMoisture2() {
        return moisture2;
    }

    public void setMoisture2(double moisture2) {
        this.moisture2 = moisture2;
    }

    public double getMoisture3() {
        return moisture3;
    }

    public void setMoisture3(double moisture3) {
        this.moisture3 = moisture3;
    }

    public double getMoisture4() {
        return moisture4;
    }

    public void setMoisture4(double moisture4) {
        this.moisture4 = moisture4;
    }

    public boolean isLed() {
        return led;
    }

    public void setLed(boolean led) {
        this.led = led;
    }

    public boolean isPump1() {
        return pump1;
    }

    public void setPump1(boolean pump1) {
        this.pump1 = pump1;
    }

    public boolean isPump2() {
        return pump2;
    }

    public void setPump2(boolean pump2) {
        this.pump2 = pump2;
    }

    public boolean isPump1Triggered() {
        return pump1Triggered;
    }

    public void setPump1Triggered(boolean pump1Triggered) {
        this.pump1Triggered = pump1Triggered;
    }

    public boolean isPump2Triggered() {
        return pump2Triggered;
    }

    public void setPump2Triggered(boolean pump2Triggered) {
        this.pump2Triggered = pump2Triggered;
    }

    public boolean isTank() {
        return tank;
    }

    public void setTank(boolean tank) {
        this.tank = tank;
    }

    public boolean isPipe() {
        return pipe;
    }

    public void setPipe(boolean pipe) {
        this.pipe = pipe;
    }

    public EmbeddedProfile getProfile() {
        return profile;
    }

    public void setProfile(EmbeddedProfile profile) {
        this.profile = profile;
    }

    public List<String> getSystemAlerts() {
        return systemAlerts;
    }

    public void setSystemAlerts(List<String> systemAlerts) {
        this.systemAlerts = systemAlerts;
    }

    public static class EmbeddedProfile {

        @SerializedName("moist_upper")
        private int moistUpper;

        @SerializedName("moist_lower")
        private int moistLower;

        @SerializedName("temp_upper")
        private int tempUpper;

        @SerializedName("temp_lower")
        private int tempLower;

        @SerializedName("hum_upper")
        private int humUpper;

        @SerializedName("hum_lower")
        private int humLower;

        @SerializedName("light_threshold")
        private int lightThreshold;

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
}
