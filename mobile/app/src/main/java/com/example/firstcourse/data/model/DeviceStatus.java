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

    public List<String> getSystemAlerts() {
        return systemAlerts;
    }

    public void setSystemAlerts(List<String> systemAlerts) {
        this.systemAlerts = systemAlerts;
    }
}
