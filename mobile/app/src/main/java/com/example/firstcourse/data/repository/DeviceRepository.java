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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceRepository {

    private static final boolean USE_MOCK_DATA = false;

    // Synced with embedded/src/networking.c enum Commands (0-13)
    private static final int LED_ON = 0;
    private static final int LED_OFF = 1;
    private static final int PUMP_ON = 2;
    private static final int PUMP_OFF = 3;
    private static final int LED_READ = 4;
    private static final int TEMP_READ = 5;
    private static final int HUMIDITY_READ = 6;
    private static final int LIGHT_READ = 7;
    private static final int MOISTURE_READ = 8;
    private static final int FLOW_READ = 9;
    private static final int STATUS_GENERAL = 10;
    private static final int STATUS_LED = 11;
    private static final int STATUS_PUMP = 12;
    private static final int PROFILE = 13;

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
        } else {
            enqueueCommand(STATUS_GENERAL, new CommandHandler() {
                @Override
                public void onSuccess(String body) {
                    if (!hasStructuredDeviceStatusFields(body)) {
                        data.setValue(ApiResult.error(
                                isAckOnlyResponse(body)
                                        ? "ESP32 returned acknowledgement only. Structured device status is not exposed yet."
                                        : "Device status could not be parsed: " + safeBody(body),
                                ApiResult.FailureReason.PARSE_ERROR));
                        return;
                    }

                    DeviceStatus status = parseDeviceStatus(body);
                    data.setValue(ApiResult.success(status, "Device status updated."));
                }

                @Override
                public void onFailure(ApiResult.FailureReason reason, String message) {
                    data.setValue(ApiResult.error(message, reason));
                }
            });
        }
        return data;
    }

    public LiveData<ApiResult<IrrigationResponse>> irrigateNow() {
        MutableLiveData<ApiResult<IrrigationResponse>> data = new MutableLiveData<>();
        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(MockData.getMockIrrigationResponse(), "Mock irrigation command succeeded."));
        } else {
            enqueueCommand(PUMP_ON, new CommandHandler() {
                @Override
                public void onSuccess(String body) {
                    IrrigationResponse irrigationResponse = new IrrigationResponse();
                    irrigationResponse.setMessage("ESP32 response: " + safeBody(body));
                    data.setValue(ApiResult.success(irrigationResponse, "Irrigation command sent."));
                }

                @Override
                public void onFailure(ApiResult.FailureReason reason, String message) {
                    data.setValue(ApiResult.error(message, reason));
                }
            });
        }
        return data;
    }

    public LiveData<ApiResult<IrrigationResponse>> stopIrrigation() {
        MutableLiveData<ApiResult<IrrigationResponse>> data = new MutableLiveData<>();
        enqueueCommand(PUMP_OFF, new CommandHandler() {
            @Override
            public void onSuccess(String body) {
                IrrigationResponse response = new IrrigationResponse();
                response.setMessage("Pump stopped: " + safeBody(body));
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
        int command = turnOn ? LED_ON : LED_OFF;

        enqueueCommand(command, new CommandHandler() {
            @Override
            public void onSuccess(String body) {
                data.setValue(ApiResult.success(turnOn, "LED command applied: " + safeBody(body)));
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

        enqueueCommand(LED_READ, new CommandHandler() {
            @Override
            public void onSuccess(String body) {
                Boolean parsed = parseBoolean(body);
                if (parsed != null) {
                    data.setValue(ApiResult.success(parsed, "LED status received: " + safeBody(body)));
                    return;
                }

                data.setValue(ApiResult.error(
                        isAckOnlyResponse(body)
                                ? "ESP32 returned acknowledgement only. LED state is not exposed yet."
                                : "LED status could not be parsed: " + safeBody(body),
                        ApiResult.FailureReason.PARSE_ERROR));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<Double>> readTemperature() {
        return readSensorDouble(TEMP_READ, "temperature", "Temperature read.");
    }

    public LiveData<ApiResult<Double>> readHumidity() {
        return readSensorDouble(HUMIDITY_READ, "humidity", "Humidity read.");
    }

    public LiveData<ApiResult<Double>> readLightIntensity() {
        return readSensorDouble(LIGHT_READ, "light", "Light intensity read.");
    }

    public LiveData<ApiResult<Double>> readSoilMoisture() {
        return readSensorDouble(MOISTURE_READ, "soil_moisture", "Soil moisture read.");
    }

    public LiveData<ApiResult<Double>> readFlow() {
        return readSensorDouble(FLOW_READ, "flow", "Flow rate read.");
    }

    public LiveData<ApiResult<String>> getGeneralStatusRaw() {
        return executeRawCommand(STATUS_GENERAL, "General status received.");
    }

    public LiveData<ApiResult<String>> getLedStatusRaw() {
        return executeRawCommand(STATUS_LED, "LED system status received.");
    }

    public LiveData<ApiResult<String>> getPumpStatusRaw() {
        return executeRawCommand(STATUS_PUMP, "Pump system status received.");
    }

    public LiveData<ApiResult<String>> getProfileRaw() {
        return executeRawCommand(PROFILE, "Profile data received.");
    }

    /**
     * Fetches the list of irrigation profiles.
     * @return A LiveData object containing the list of profiles.
     */
    public LiveData<ApiResult<List<IrrigationProfile>>> getIrrigationProfiles() {
        MutableLiveData<ApiResult<List<IrrigationProfile>>> data = new MutableLiveData<>();
        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(MockData.getMockIrrigationProfiles(), "Mock profile list loaded."));
        } else {
            enqueueCommand(PROFILE, new CommandHandler() {
                @Override
                public void onSuccess(String body) {
                    List<IrrigationProfile> parsed = parseProfiles(body);
                    String message = parsed.isEmpty() && isAckOnlyResponse(body)
                            ? "ESP32 returned acknowledgement only. Profile list is not exposed yet."
                            : "Profile list updated.";
                    data.setValue(ApiResult.success(parsed, message));
                }

                @Override
                public void onFailure(ApiResult.FailureReason reason, String message) {
                    data.setValue(ApiResult.error(message, reason));
                }
            });
        }
        return data;
    }

    /**
     * Creates a new irrigation profile.
     * @param profile The profile to create.
     * @return A LiveData object containing the created profile from the server.
     */
    public LiveData<ApiResult<IrrigationProfile>> createIrrigationProfile(IrrigationProfile profile) {
        MutableLiveData<ApiResult<IrrigationProfile>> data = new MutableLiveData<>();
        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(profile, "Mock profile created."));
        } else {
            enqueueCommand(PROFILE, new CommandHandler() {
                @Override
                public void onSuccess(String body) {
                    if (isAckSuccessful(body)) {
                        data.setValue(ApiResult.success(profile, "Profile confirmed by device."));
                    } else {
                        data.setValue(ApiResult.error(
                                "Profile could not be saved. Device confirmation was not received.",
                                ApiResult.FailureReason.SERVER_ERROR));
                    }
                }

                @Override
                public void onFailure(ApiResult.FailureReason reason, String message) {
                    data.setValue(ApiResult.error(message, reason));
                }
            });
        }
        return data;
    }

    private LiveData<ApiResult<Double>> readSensorDouble(int command, String keyHint, String successMessage) {
        MutableLiveData<ApiResult<Double>> data = new MutableLiveData<>();
        enqueueCommand(command, new CommandHandler() {
            @Override
            public void onSuccess(String body) {
                Double value = parseDouble(body, keyHint);
                if (value == null) {
                    data.setValue(ApiResult.error(
                            isAckOnlyResponse(body)
                                    ? "ESP32 returned acknowledgement only. " + keyHint + " is not exposed yet."
                                    : "Sensor response could not be parsed: " + safeBody(body),
                            ApiResult.FailureReason.PARSE_ERROR));
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

    private LiveData<ApiResult<String>> executeRawCommand(int command, String successMessage) {
        MutableLiveData<ApiResult<String>> data = new MutableLiveData<>();
        enqueueCommand(command, new CommandHandler() {
            @Override
            public void onSuccess(String body) {
                data.setValue(ApiResult.success(safeBody(body), successMessage));
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });
        return data;
    }

    private void enqueueCommand(int command, CommandHandler handler) {
        apiService.sendCommand(command).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
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
            public void onFailure(Call<String> call, Throwable t) {
                handler.onFailure(resolveFailureReason(t), resolveFailureMessage(t));
            }
        });
    }

    private DeviceStatus parseDeviceStatus(String raw) {
        DeviceStatus status = new DeviceStatus();
        status.setSoilMoisture(defaultValue(parseDouble(raw, "soil_moisture", "moisture"), 0.0));
        status.setTemperature(defaultValue(parseDouble(raw, "temperature", "temp"), 0.0));
        status.setHumidity(defaultValue(parseDouble(raw, "humidity"), 0.0));
        status.setLightIntensity((int) Math.round(defaultValue(parseDouble(raw, "light_intensity", "light"), 0.0)));
        status.setBatteryPercentage((int) Math.round(defaultValue(parseDouble(raw, "battery_percentage", "battery"), 0.0)));

        List<String> alerts = new ArrayList<>();
        alerts.add("Raw response: " + safeBody(raw));
        if (isAckOnlyResponse(raw)) {
            alerts.add("Structured device status is not exposed by the firmware yet.");
        }
        status.setSystemAlerts(alerts);
        return status;
    }

    private List<IrrigationProfile> parseProfiles(String raw) {
        List<IrrigationProfile> profiles = new ArrayList<>();

        if (raw == null || raw.trim().isEmpty()) {
            return profiles;
        }

        try {
            JsonElement element = new JsonParser().parse(raw);
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> {
                    if (!item.isJsonObject()) {
                        return;
                    }
                    JsonObject o = item.getAsJsonObject();
                    String plant = getString(o, "plant_name", "plant", "name");
                    int timesPerDay = (int) Math.round(defaultValue(getDouble(o, "times_per_day"), 0.0));
                    int waterAmount = (int) Math.round(defaultValue(getDouble(o, "water_amount"), 0.0));

                    List<Integer> times = new ArrayList<>();
                    if (o.has("times_of_day") && o.get("times_of_day").isJsonArray()) {
                        o.getAsJsonArray("times_of_day").forEach(t -> {
                            try {
                                times.add(t.getAsInt());
                            } catch (Exception ignored) {
                            }
                        });
                    }

                    profiles.add(new IrrigationProfile(plant, timesPerDay, times, waterAmount));
                });
                return profiles;
            }
        } catch (Exception ignored) {
            // Fallback to mock data shape if device returns non-JSON.
        }

        return profiles;
    }

    private boolean isAckSuccessful(String body) {
        String normalized = safeBody(body).toLowerCase(Locale.ROOT);
        return normalized.contains("ok")
                || normalized.contains("success")
                || normalized.contains("created")
                || normalized.contains("done");
    }

    private String safeBody(String body) {
        return body == null ? "" : body.trim();
    }

    private Double parseDouble(String raw, String... keyHints) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        // Try JSON first.
        try {
            JsonElement element = new JsonParser().parse(raw);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                for (String keyHint : keyHints) {
                    Double v = getDouble(obj, keyHint);
                    if (v != null) {
                        return v;
                    }
                }
                Double fallback = getFirstNumericValue(obj);
                if (fallback != null) {
                    return fallback;
                }
            }
        } catch (Exception ignored) {
            // Not JSON; continue with text parsing.
        }

        // key=value parsing.
        for (String keyHint : keyHints) {
            Pattern p = Pattern.compile("(?i)" + Pattern.quote(keyHint) + "\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?)");
            Matcher m = p.matcher(raw);
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Last resort: first number in response.
        Matcher any = Pattern.compile("-?\\d+(?:\\.\\d+)?").matcher(raw);
        if (any.find()) {
            try {
                return Double.parseDouble(any.group());
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }

    private Double getFirstNumericValue(JsonObject obj) {
        for (String key : obj.keySet()) {
            try {
                JsonElement element = obj.get(key);
                if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsDouble();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Double getDouble(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (!obj.has(key) || obj.get(key) == null) {
                continue;
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
        }
        return null;
    }

    private String getString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (!obj.has(key) || obj.get(key) == null) {
                continue;
            }
            try {
                return obj.get(key).getAsString();
            } catch (Exception ignored) {
            }
        }
        return "Unknown";
    }

    private Boolean parseBoolean(String body) {
        String normalized = safeBody(body).toLowerCase(Locale.ROOT);
        if (normalized.matches(".*\\b(on|true|1)\\b.*")) {
            return true;
        }
        if (normalized.matches(".*\\b(off|false|0)\\b.*")) {
            return false;
        }
        return null;
    }

    private boolean isAckOnlyResponse(String body) {
        String normalized = safeBody(body).toLowerCase(Locale.ROOT);
        return "ok".equals(normalized)
                || normalized.startsWith("ok ")
                || normalized.startsWith("ok:")
                || normalized.contains("ack")
                || normalized.contains("command executed")
                || normalized.contains("device confirmation")
                || normalized.contains("success")
                || normalized.contains("done");
    }

    private boolean hasStructuredDeviceStatusFields(String body) {
        return parseDouble(body, "soil_moisture", "moisture") != null
                || parseDouble(body, "temperature", "temp") != null
                || parseDouble(body, "humidity") != null
                || parseDouble(body, "light_intensity", "light") != null
                || parseDouble(body, "battery_percentage", "battery") != null;
    }

    private double defaultValue(Double value, double fallback) {
        return value == null ? fallback : value;
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

    private interface CommandHandler {
        void onSuccess(String body);

        void onFailure(ApiResult.FailureReason reason, String message);
    }
}
