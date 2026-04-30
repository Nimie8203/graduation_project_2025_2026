package com.example.firstcourse.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.firstcourse.data.mock.MockData;
import com.example.firstcourse.data.model.ApiResult;
import com.example.firstcourse.data.model.DeviceStatus;
import com.example.firstcourse.data.model.IrrigationProfile;
import com.example.firstcourse.data.model.IrrigationResponse;
import com.example.firstcourse.data.remote.ApiService;
import com.example.firstcourse.data.remote.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceRepository {

    private static final boolean USE_MOCK_DATA = false;
    private static final int DEFAULT_MOISTURE_THRESHOLD = 30;

    private static DeviceRepository instance;
    private final ApiService apiService;

    private DeviceRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public static synchronized DeviceRepository getInstance() {
        if (instance == null) {
            instance = new DeviceRepository();
        }
        return instance;
    }

    public LiveData<ApiResult<DeviceStatus>> getDeviceStatus() {
        MutableLiveData<ApiResult<DeviceStatus>> data = new MutableLiveData<>();

        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(MockData.getMockDeviceStatus(), "Mock device status loaded."));
            return data;
        }

        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Device status request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                data.setValue(ApiResult.success(parseDeviceStatus(body), "Device status updated."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<IrrigationResponse>> irrigateNow() {
        MutableLiveData<ApiResult<IrrigationResponse>> data = new MutableLiveData<>();

        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(MockData.getMockIrrigationResponse(), "Mock irrigation command succeeded."));
            return data;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("source", "mobile");
        payload.addProperty("state", "on");

        enqueueJson(apiService.setPumpState(payload), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Pump start command failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                IrrigationResponse response = new IrrigationResponse();
                response.setMessage("Pump started for mobile source.");
                data.setValue(ApiResult.success(response, "Irrigation command sent."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<IrrigationResponse>> stopIrrigation() {
        MutableLiveData<ApiResult<IrrigationResponse>> data = new MutableLiveData<>();

        JsonObject payload = new JsonObject();
        payload.addProperty("source", "mobile");
        payload.addProperty("state", "off");

        enqueueJson(apiService.setPumpState(payload), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Pump stop command failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                IrrigationResponse response = new IrrigationResponse();
                response.setMessage("Pump stopped for mobile source.");
                data.setValue(ApiResult.success(response, "Pump stop command sent."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<Boolean>> setLedStatus(boolean turnOn) {
        MutableLiveData<ApiResult<Boolean>> data = new MutableLiveData<>();

        JsonObject payload = new JsonObject();
        payload.addProperty("state", turnOn ? "on" : "off");

        enqueueJson(apiService.setLedState(payload), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "LED command failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                Boolean parsed = parseStateBoolean(body.get("state"));
                boolean resolved = parsed != null ? parsed : turnOn;
                data.setValue(ApiResult.success(resolved, "LED command applied."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<Boolean>> getLedStatus() {
        MutableLiveData<ApiResult<Boolean>> data = new MutableLiveData<>();

        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "LED status request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                Boolean led = getBoolean(body, "led");
                if (led == null) {
                    data.setValue(ApiResult.error("LED field is missing in /api/status response.", ApiResult.FailureReason.PARSE_ERROR));
                    return;
                }

                data.setValue(ApiResult.success(led, "LED status received."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<Double>> readTemperature() {
        MutableLiveData<ApiResult<Double>> data = new MutableLiveData<>();
        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Temperature request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                Double raw = getNumber(body, "temperature");
                if (raw == null) {
                    data.setValue(ApiResult.error("temperature field is missing in /api/status response.", ApiResult.FailureReason.PARSE_ERROR));
                    return;
                }

                data.setValue(ApiResult.success(normalizeTenths(raw), "Temperature read."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<Double>> readHumidity() {
        MutableLiveData<ApiResult<Double>> data = new MutableLiveData<>();
        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Humidity request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                Double raw = getNumber(body, "humidity");
                if (raw == null) {
                    data.setValue(ApiResult.error("humidity field is missing in /api/status response.", ApiResult.FailureReason.PARSE_ERROR));
                    return;
                }

                data.setValue(ApiResult.success(normalizeTenths(raw), "Humidity read."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<Double>> readLightIntensity() {
        return readSensorFromStatus("light_intensity", "Light intensity read.");
    }

    public LiveData<ApiResult<Double>> readSoilMoisture() {
        MutableLiveData<ApiResult<Double>> data = new MutableLiveData<>();
        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Moisture request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                Double moisture = averageMoisture(body);
                if (moisture == null) {
                    data.setValue(ApiResult.error("Moisture fields are missing in /api/status response.", ApiResult.FailureReason.PARSE_ERROR));
                    return;
                }

                data.setValue(ApiResult.success(moisture, "Soil moisture read."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<Double>> readFlow() {
        MutableLiveData<ApiResult<Double>> data = new MutableLiveData<>();
        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Flow request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                Double flow1 = getNumber(body, "flow_sensor_1");
                Double flow2 = getNumber(body, "flow_sensor_2");

                if (flow1 == null && flow2 == null) {
                    data.setValue(ApiResult.error("Flow fields are missing in /api/status response.", ApiResult.FailureReason.PARSE_ERROR));
                    return;
                }

                double value;
                if (flow1 != null && flow2 != null) {
                    value = (flow1 + flow2) / 2.0;
                } else if (flow1 != null) {
                    value = flow1;
                } else {
                    value = flow2;
                }

                data.setValue(ApiResult.success(value, "Flow rate read."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<String>> getGeneralStatusRaw() {
        MutableLiveData<ApiResult<String>> data = new MutableLiveData<>();
        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                data.setValue(ApiResult.success(body.toString(), "General status received."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<String>> getLedStatusRaw() {
        MutableLiveData<ApiResult<String>> data = new MutableLiveData<>();
        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                JsonObject ledStatus = new JsonObject();
                ledStatus.add("led", body.get("led"));
                ledStatus.add("status", body.get("status"));
                data.setValue(ApiResult.success(ledStatus.toString(), "LED system status received."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<String>> getPumpStatusRaw() {
        MutableLiveData<ApiResult<String>> data = new MutableLiveData<>();
        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                JsonObject pumpStatus = new JsonObject();
                pumpStatus.add("pump_1", body.get("pump_1"));
                pumpStatus.add("pump_2", body.get("pump_2"));
                pumpStatus.add("status", body.get("status"));
                data.setValue(ApiResult.success(pumpStatus.toString(), "Pump system status received."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<String>> getProfileRaw() {
        MutableLiveData<ApiResult<String>> data = new MutableLiveData<>();
        enqueueJson(apiService.getProfile(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                data.setValue(ApiResult.success(body.toString(), "Profile data received."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    public LiveData<ApiResult<List<IrrigationProfile>>> getIrrigationProfiles() {
        MutableLiveData<ApiResult<List<IrrigationProfile>>> data = new MutableLiveData<>();

        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(MockData.getMockIrrigationProfiles(), "Mock profile list loaded."));
            return data;
        }

        enqueueJson(apiService.getProfile(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (isNoActiveProfile(body)) {
                    data.setValue(ApiResult.success(new ArrayList<>(), getDeviceMessage(body, "No active profile.")));
                    return;
                }

                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Profile list request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                IrrigationProfile profile = parseProfile(body);
                if (profile == null) {
                    data.setValue(ApiResult.error("Profile payload could not be parsed.", ApiResult.FailureReason.PARSE_ERROR));
                    return;
                }

                List<IrrigationProfile> list = new ArrayList<>();
                list.add(profile);
                data.setValue(ApiResult.success(list, "Profile list updated."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<IrrigationProfile>> createIrrigationProfile(IrrigationProfile profile) {
        MutableLiveData<ApiResult<IrrigationProfile>> data = new MutableLiveData<>();

        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(profile, "Mock profile created."));
            return data;
        }

        if (profile == null || profile.getPlantName() == null || profile.getPlantName().trim().isEmpty()) {
            data.setValue(ApiResult.error("Plant name is required.", ApiResult.FailureReason.PARSE_ERROR));
            return data;
        }

        if (profile.getTimesOfDay() == null || profile.getTimesOfDay().isEmpty()) {
            data.setValue(ApiResult.error("times_of_day must include at least one value.", ApiResult.FailureReason.PARSE_ERROR));
            return data;
        }

        JsonObject payload = new JsonObject();
        String profileName = profile.getProfileName();
        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = profile.getPlantName().trim() + " Profile";
        }

        payload.addProperty("profile_name", profileName.trim());
        payload.addProperty("plant_name", profile.getPlantName().trim());
        payload.addProperty("irrig_times_per_day", profile.getTimesPerDay());
        payload.addProperty("water_amount_per_irrig", profile.getWaterAmount());
        payload.addProperty("moisture_threshold", DEFAULT_MOISTURE_THRESHOLD);

        JsonArray times = new JsonArray();
        for (Integer time : profile.getTimesOfDay()) {
            if (time != null) {
                times.add(time);
            }
        }
        payload.add("times_of_day", times);

        enqueueJson(apiService.createProfile(payload), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Profile could not be saved."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                data.setValue(ApiResult.success(profile, "Profile confirmed by device."));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    private LiveData<ApiResult<Double>> readSensorFromStatus(String fieldName, String successMessage) {
        MutableLiveData<ApiResult<Double>> data = new MutableLiveData<>();

        enqueueJson(apiService.getStatus(), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Sensor request failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                Double value = getNumber(body, fieldName);
                if (value == null) {
                    data.setValue(ApiResult.error(fieldName + " field is missing in /api/status response.", ApiResult.FailureReason.PARSE_ERROR));
                    return;
                }

                data.setValue(ApiResult.success(value, successMessage));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    private DeviceStatus parseDeviceStatus(JsonObject body) {
        DeviceStatus status = new DeviceStatus();
        status.setTemperature(normalizeTenths(defaultValue(getNumber(body, "temperature"), 0.0)));
        status.setHumidity(normalizeTenths(defaultValue(getNumber(body, "humidity"), 0.0)));
        status.setLightIntensity((int) Math.round(defaultValue(getNumber(body, "light_intensity"), 0.0)));
        status.setFlowSensor1(defaultValue(getNumber(body, "flow_sensor_1"), 0.0));
        status.setFlowSensor2(defaultValue(getNumber(body, "flow_sensor_2"), 0.0));
        status.setMoisture1(defaultValue(getNumber(body, "moisture_1"), 0.0));
        status.setMoisture2(defaultValue(getNumber(body, "moisture_2"), 0.0));
        status.setMoisture3(defaultValue(getNumber(body, "moisture_3"), 0.0));
        status.setMoisture4(defaultValue(getNumber(body, "moisture_4"), 0.0));

        Double moisture = averageMoisture(body);
        double avgMoisture = defaultValue(moisture, 0.0);

        List<String> alerts = new ArrayList<>();
        alerts.add(String.format(Locale.getDefault(), "Avg moisture=%.1f%%", avgMoisture));
        alerts.add(String.format(Locale.getDefault(), "LED=%s, Pump1=%s, Pump2=%s",
                String.valueOf(getBoolean(body, "led")),
                String.valueOf(getBoolean(body, "pump_1")),
                String.valueOf(getBoolean(body, "pump_2"))));
        status.setSystemAlerts(alerts);

        return status;
    }

    private IrrigationProfile parseProfile(JsonObject body) {
        String profileName = getString(body, "profile_name");
        String plantName = getString(body, "plant_name");
        Double timesPerDayRaw = getNumber(body, "irrig_times_per_day");
        Double waterAmountRaw = getNumber(body, "water_amount_per_irrig");

        if (plantName == null || timesPerDayRaw == null || waterAmountRaw == null) {
            return null;
        }

        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = plantName + " Profile";
        }

        List<Integer> times = new ArrayList<>();
        JsonElement todElement = body.get("times_of_day");
        if (todElement != null && todElement.isJsonArray()) {
            for (JsonElement e : todElement.getAsJsonArray()) {
                try {
                    times.add(e.getAsInt());
                } catch (Exception ignored) {
                }
            }
        }

        return new IrrigationProfile(
            profileName,
                plantName,
                (int) Math.round(timesPerDayRaw),
                times,
                (int) Math.round(waterAmountRaw)
        );
    }

    private Double averageMoisture(JsonObject body) {
        double total = 0.0;
        int count = 0;

        for (String key : new String[]{"moisture_1", "moisture_2", "moisture_3", "moisture_4"}) {
            Double value = getNumber(body, key);
            if (value != null) {
                total += value;
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        return total / count;
    }

    private void enqueueJson(Call<JsonObject> call, JsonHandler handler) {
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handler.onSuccess(response.body());
                    return;
                }

                handler.onFailure(
                        ApiResult.FailureReason.SERVER_ERROR,
                        "Device is not responding or returned an invalid response."
                );
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handler.onFailure(resolveFailureReason(t), resolveFailureMessage(t));
            }
        });
    }

    private boolean isStatusOk(JsonObject body) {
        String status = getString(body, "status");
        return status != null && "ok".equalsIgnoreCase(status);
    }

    private boolean isNoActiveProfile(JsonObject body) {
        String status = getString(body, "status");
        String msg = getString(body, "msg");
        return status != null
                && "error".equalsIgnoreCase(status)
                && msg != null
                && msg.toLowerCase(Locale.ROOT).contains("no active profile");
    }

    private String getDeviceMessage(JsonObject body, String fallback) {
        String msg = getString(body, "msg");
        return msg != null && !msg.trim().isEmpty() ? msg : fallback;
    }

    private Double getNumber(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key) || obj.get(key) == null) {
            return null;
        }

        try {
            JsonElement element = obj.get(key);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return element.getAsDouble();
            }
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return Double.parseDouble(element.getAsString());
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private Boolean getBoolean(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key) || obj.get(key) == null) {
            return null;
        }

        try {
            JsonElement element = obj.get(key);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean();
            }
            return parseStateBoolean(element);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Boolean parseStateBoolean(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }

        try {
            if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean();
            }
            if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt() != 0;
            }
            if (element.getAsJsonPrimitive().isString()) {
                String normalized = element.getAsString().trim().toLowerCase(Locale.ROOT);
                if ("on".equals(normalized) || "true".equals(normalized) || "1".equals(normalized)) {
                    return true;
                }
                if ("off".equals(normalized) || "false".equals(normalized) || "0".equals(normalized)) {
                    return false;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String getString(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key) || obj.get(key) == null) {
            return null;
        }

        try {
            return obj.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private double defaultValue(Double value, double fallback) {
        return value == null ? fallback : value;
    }

    private double normalizeTenths(double value) {
        return Math.abs(value) >= 100.0 ? value / 10.0 : value;
    }

    private ApiResult.FailureReason resolveFailureReason(Throwable t) {
        if (t instanceof UnknownHostException || t instanceof ConnectException) {
            return ApiResult.FailureReason.NOT_CONNECTED;
        }
        if (t instanceof SocketTimeoutException) {
            return ApiResult.FailureReason.DEVICE_NOT_RESPONDING;
        }
        return ApiResult.FailureReason.UNKNOWN;
    }

    private String resolveFailureMessage(Throwable t) {
        if (t instanceof UnknownHostException || t instanceof ConnectException) {
            return "You are not connected to the ESP32 network.";
        }
        if (t instanceof SocketTimeoutException) {
            return "Device is not responding.";
        }
        return "Connection error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
    }

    private interface JsonHandler {
        void onSuccess(JsonObject body);

        void onFailure(ApiResult.FailureReason reason, String message);
    }
}
