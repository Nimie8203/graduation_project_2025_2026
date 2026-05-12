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
            long currentTimeMs = System.currentTimeMillis();
            data.setValue(ApiResult.success(MockData.getDynamicDeviceStatus(currentTimeMs), "Mock device status loaded."));
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

        // Send ON command to both pumps and aggregate results.
        JsonObject p1 = new JsonObject();
        p1.addProperty("source", "mobile");
        p1.addProperty("state", "on");
        p1.addProperty("pump", 1);

        JsonObject p2 = new JsonObject();
        p2.addProperty("source", "mobile");
        p2.addProperty("state", "on");
        p2.addProperty("pump", 2);

        final int[] successCount = {0};
        final String[] errorMsg = {null};

        enqueueJson(apiService.setPumpState(p1), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                successCount[0]++;
                if (successCount[0] == 2) {
                    IrrigationResponse response = new IrrigationResponse();
                    response.setMessage("Both pumps started.");
                    data.setValue(ApiResult.success(response, "Irrigation command sent to both pumps."));
                }
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                errorMsg[0] = message;
                data.setValue(ApiResult.error(message, reason));
            }
        });

        enqueueJson(apiService.setPumpState(p2), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                successCount[0]++;
                if (successCount[0] == 2) {
                    IrrigationResponse response = new IrrigationResponse();
                    response.setMessage("Both pumps started.");
                    data.setValue(ApiResult.success(response, "Irrigation command sent to both pumps."));
                }
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                errorMsg[0] = message;
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    public LiveData<ApiResult<IrrigationResponse>> startPump1() {
        return setPumpCommand("pump_1", true, "Pump 1 command sent.", "Pump 1 started.");
    }

    public LiveData<ApiResult<IrrigationResponse>> stopPump1() {
        return setPumpCommand("pump_1", false, "Pump 1 stop command sent.", "Pump 1 stopped.");
    }

    public LiveData<ApiResult<IrrigationResponse>> startPump2() {
        return setPumpCommand("pump_2", true, "Pump 2 command sent.", "Pump 2 started.");
    }

    public LiveData<ApiResult<IrrigationResponse>> stopPump2() {
        return setPumpCommand("pump_2", false, "Pump 2 stop command sent.", "Pump 2 stopped.");
    }

    public LiveData<ApiResult<IrrigationResponse>> stopIrrigation() {
        MutableLiveData<ApiResult<IrrigationResponse>> data = new MutableLiveData<>();

        JsonObject pump1 = new JsonObject();
        pump1.addProperty("pump", 1);
        pump1.addProperty("state", "off");

        JsonObject pump2 = new JsonObject();
        pump2.addProperty("pump", 2);
        pump2.addProperty("state", "off");

        final int[] successCount = {0};

        enqueueJson(apiService.setPumpState(pump1), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Pump 1 stop command failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                successCount[0]++;
                if (successCount[0] == 2) {
                    IrrigationResponse response = new IrrigationResponse();
                    response.setMessage("Both pumps stopped.");
                    data.setValue(ApiResult.success(response, "Pump stop command sent."));
                }
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        enqueueJson(apiService.setPumpState(pump2), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Pump 2 stop command failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                successCount[0]++;
                if (successCount[0] == 2) {
                    IrrigationResponse response = new IrrigationResponse();
                    response.setMessage("Both pumps stopped.");
                    data.setValue(ApiResult.success(response, "Pump stop command sent."));
                }
            }

            @Override
            public void onFailure(ApiResult.FailureReason reason, String message) {
                data.setValue(ApiResult.error(message, reason));
            }
        });

        return data;
    }

    private LiveData<ApiResult<IrrigationResponse>> setPumpCommand(String pumpId, boolean turnOn, String successMessage, String responseMessage) {
        MutableLiveData<ApiResult<IrrigationResponse>> data = new MutableLiveData<>();

        if (USE_MOCK_DATA) {
            data.setValue(ApiResult.success(MockData.getMockIrrigationResponse(), "Mock irrigation command succeeded."));
            return data;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("state", turnOn ? "on" : "off");

        // When a specific pump is requested, send numeric pump id (1 or 2).
        // Avoid sending `source` when specifying `pump` because the firmware
        // interprets `source` strings ("web"/"mobile") as pump selectors.
        if (pumpId != null && pumpId.startsWith("pump_")) {
            int num = pumpId.endsWith("_2") ? 2 : 1;
            payload.addProperty("pump", num);
        } else {
            // Fallback: if pumpId isn't of form pump_1/pump_2, include source as mobile
            payload.addProperty("source", "mobile");
        }

        enqueueJson(apiService.setPumpState(payload), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (!isStatusOk(body)) {
                    data.setValue(ApiResult.error(getDeviceMessage(body, "Pump command failed."), ApiResult.FailureReason.SERVER_ERROR));
                    return;
                }

                IrrigationResponse response = new IrrigationResponse();
                response.setMessage(responseMessage);
                data.setValue(ApiResult.success(response, successMessage));
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

        data.setValue(ApiResult.success(MockData.getMockIrrigationProfiles(), "Profile list loaded from mobile storage."));
        return data;
    }

    public LiveData<ApiResult<IrrigationProfile>> createIrrigationProfile(IrrigationProfile profile) {
        MutableLiveData<ApiResult<IrrigationProfile>> data = new MutableLiveData<>();
        if (profile == null || profile.getPlantName() == null || profile.getPlantName().trim().isEmpty()) {
            data.setValue(ApiResult.error("Plant name is required.", ApiResult.FailureReason.PARSE_ERROR));
            return data;
        }

        String profileName = profile.getProfileName();
        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = profile.getPlantName().trim() + " Profile";
            profile.setProfileName(profileName);
        }

        MockData.addMockProfile(profile);
        data.setValue(ApiResult.success(profile, "Profile saved on mobile."));
        return data;
    }

    public LiveData<ApiResult<Void>> deleteIrrigationProfile(String profileName) {
        MutableLiveData<ApiResult<Void>> data = new MutableLiveData<>();
        if (profileName == null || profileName.trim().isEmpty()) {
            data.setValue(ApiResult.error("Profile name is required.", ApiResult.FailureReason.PARSE_ERROR));
            return data;
        }
        MockData.removeMockProfile(profileName.trim());
        data.setValue(ApiResult.success(null, "Profile deleted from mobile."));
        return data;
    }

    public LiveData<ApiResult<Void>> activateIrrigationProfile(IrrigationProfile profile) {
        MutableLiveData<ApiResult<Void>> data = new MutableLiveData<>();

        if (profile == null || profile.getPlantName() == null || profile.getPlantName().trim().isEmpty()) {
            data.setValue(ApiResult.error("Plant name is required.", ApiResult.FailureReason.PARSE_ERROR));
            return data;
        }

        JsonObject payload = new JsonObject();

        // Send band values directly to firmware
        payload.addProperty("moist_upper", clampInt(profile.getMoistUpper(), 0, 100));
        payload.addProperty("moist_lower", clampInt(profile.getMoistLower(), 0, 100));
        payload.addProperty("temp_upper", clampInt(profile.getTempUpper(), -100, 100));
        payload.addProperty("temp_lower", clampInt(profile.getTempLower(), -100, 100));
        payload.addProperty("hum_upper", clampInt(profile.getHumUpper(), 0, 100));
        payload.addProperty("hum_lower", clampInt(profile.getHumLower(), 0, 100));
        payload.addProperty("light_threshold", clampInt(profile.getLightThreshold(), 0, 100));

        enqueueJson(apiService.createProfile(payload), new JsonHandler() {
            @Override
            public void onSuccess(JsonObject body) {
                if (body != null) {
                    String status = getString(body, "status");
                    if (status != null && "error".equalsIgnoreCase(status)) {
                        data.setValue(ApiResult.error(getDeviceMessage(body, "Profile activation failed."), ApiResult.FailureReason.SERVER_ERROR));
                        return;
                    }
                }

                data.setValue(ApiResult.success(null, "Profile thresholds sent to device."));
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
        status.setLed(Boolean.TRUE.equals(getBoolean(body, "led")));
        status.setPump1(Boolean.TRUE.equals(getBoolean(body, "pump_1")));
        status.setPump2(Boolean.TRUE.equals(getBoolean(body, "pump_2")));
        status.setPump1Triggered(Boolean.TRUE.equals(getBoolean(body, "pump_1_triggered")));
        status.setPump2Triggered(Boolean.TRUE.equals(getBoolean(body, "pump_2_triggered")));
        status.setTank(Boolean.TRUE.equals(getBoolean(body, "tank")));
        status.setPipe(Boolean.TRUE.equals(getBoolean(body, "pipe")));
        status.setProfile(parseEmbeddedProfile(body));

        Double moisture = averageMoisture(body);
        double avgMoisture = defaultValue(moisture, 0.0);

        List<String> alerts = new ArrayList<>();
        alerts.add(String.format(Locale.getDefault(), "Avg moisture=%.1f%%", avgMoisture));
        alerts.add(String.format(Locale.getDefault(), "LED=%s, Pump1=%s, Pump2=%s",
                String.valueOf(getBoolean(body, "led")),
                String.valueOf(getBoolean(body, "pump_1")),
                String.valueOf(getBoolean(body, "pump_2"))));
        alerts.add(String.format(Locale.getDefault(), "Triggered=%s/%s, Tank=%s, Pipe=%s",
                String.valueOf(getBoolean(body, "pump_1_triggered")),
                String.valueOf(getBoolean(body, "pump_2_triggered")),
                String.valueOf(getBoolean(body, "tank")),
                String.valueOf(getBoolean(body, "pipe"))));
        status.setSystemAlerts(alerts);

        return status;
    }

    private IrrigationProfile parseProfile(JsonObject body) {
        if (body == null) {
            return null;
        }

        JsonObject source = body;
        JsonElement profileElement = body.get("profile");
        if (profileElement != null && profileElement.isJsonObject()) {
            source = profileElement.getAsJsonObject();
        }

        String profileName = getString(source, "profile_name");
        String plantName = getString(source, "plant_name");

        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = "Embedded Profile";
        }

        if (plantName == null || plantName.trim().isEmpty()) {
            plantName = "Embedded Device";
        }

        Double moistUpper = getNumber(source, "moist_upper");
        Double moistLower = getNumber(source, "moist_lower");
        Double tempUpper = getNumber(source, "temp_upper");
        Double tempLower = getNumber(source, "temp_lower");
        Double humUpper = getNumber(source, "hum_upper");
        Double humLower = getNumber(source, "hum_lower");
        Double lightRaw = getNumber(source, "light_threshold");

        int moistUpperVal = moistUpper != null ? (int) Math.round(moistUpper) : 80;
        int moistLowerVal = moistLower != null ? (int) Math.round(moistLower) : 20;
        int tempUpperVal = tempUpper != null ? (int) Math.round(tempUpper) : 40;
        int tempLowerVal = tempLower != null ? (int) Math.round(tempLower) : 5;
        int humUpperVal = humUpper != null ? (int) Math.round(humUpper) : 60;
        int humLowerVal = humLower != null ? (int) Math.round(humLower) : 20;
        int lightVal = lightRaw != null ? (int) Math.round(lightRaw) : 85;

        return new IrrigationProfile(profileName, plantName, moistUpperVal, moistLowerVal, 
            tempUpperVal, tempLowerVal, humUpperVal, humLowerVal, lightVal);
    }

    private DeviceStatus.EmbeddedProfile parseEmbeddedProfile(JsonObject body) {
        if (body == null) {
            return null;
        }

        JsonObject source = body;
        JsonElement profileElement = body.get("profile");
        if (profileElement != null && profileElement.isJsonObject()) {
            source = profileElement.getAsJsonObject();
        }

        DeviceStatus.EmbeddedProfile profile = new DeviceStatus.EmbeddedProfile();
        profile.setMoistUpper((int) Math.round(defaultValue(getNumber(source, "moist_upper"), 0.0)));
        profile.setMoistLower((int) Math.round(defaultValue(getNumber(source, "moist_lower"), 0.0)));
        profile.setTempUpper((int) Math.round(defaultValue(getNumber(source, "temp_upper"), 0.0)));
        profile.setTempLower((int) Math.round(defaultValue(getNumber(source, "temp_lower"), 0.0)));
        profile.setHumUpper((int) Math.round(defaultValue(getNumber(source, "hum_upper"), 0.0)));
        profile.setHumLower((int) Math.round(defaultValue(getNumber(source, "hum_lower"), 0.0)));
        profile.setLightThreshold((int) Math.round(defaultValue(getNumber(source, "light_threshold"), 0.0)));
        return profile;
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

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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
