#include "networking.h"
#include "pump.h"
#include "led.h"

// ─────────────────────────────────────────────
// ACTIVE PROFILE (single, in-memory)
// ─────────────────────────────────────────────

static profile_t s_profile = {0};
static bool s_profile_active = false;

// ─────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────

// Sends a cJSON object as an HTTP JSON response and frees the object.
// Ownership of `root` is consumed by this function.
static void send_json(httpd_req_t *req, cJSON *root)
{
    char *json_str = cJSON_PrintUnformatted(root);
    cJSON_Delete(root);

    if (json_str == NULL)
    {
        httpd_resp_send_err(req, HTTPD_500_INTERNAL_SERVER_ERROR, "JSON encode failed");
        return;
    }

    httpd_resp_set_type(req, "application/json");
    httpd_resp_sendstr(req, json_str);
    cJSON_free(json_str);
}

static void send_error(httpd_req_t *req, const char *msg)
{
    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "error");
    cJSON_AddStringToObject(root, "msg", msg);
    send_json(req, root);
}

static void add_cors_headers(httpd_req_t *req)
{
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Headers", "Content-Type");
}

// Reads the full POST body into a caller-supplied buffer.
// Returns ESP_OK on success, ESP_FAIL if body is too large or read fails.
static esp_err_t read_body(httpd_req_t *req, char *buf, size_t buf_size)
{
    if (req->content_len >= buf_size)
    {
        ESP_LOGI(WIFI_TAG, "POST body too large: %d bytes", req->content_len);
        return ESP_FAIL;
    }

    int received = httpd_req_recv(req, buf, req->content_len);
    if (received <= 0)
    {
        return ESP_FAIL;
    }

    buf[received] = '\0';
    return ESP_OK;
}

// ─────────────────────────────────────────────
// PREFLIGHT (OPTIONS) HANDLER
// Handles CORS preflight requests from the browser.
// ─────────────────────────────────────────────

esp_err_t options_handler(httpd_req_t *req)
{
    add_cors_headers(req);
    httpd_resp_set_status(req, "204 No Content");
    httpd_resp_send(req, NULL, 0);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// GET /api/status
// Returns all current sensor readings and device states from g_state.
// Sensors are updated by their own background tasks, not here.
// ─────────────────────────────────────────────

esp_err_t status_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddBoolToObject(root, "led", g_state.led_state);
    cJSON_AddBoolToObject(root, "pump_1", g_state.pump_1_state);
    cJSON_AddBoolToObject(root, "pump_2", g_state.pump_2_state);
    cJSON_AddNumberToObject(root, "temperature", g_state.temperature);
    cJSON_AddNumberToObject(root, "humidity", g_state.humidity);
    cJSON_AddNumberToObject(root, "light_intensity", g_state.light_intensity);
    cJSON_AddNumberToObject(root, "flow_sensor_1", g_state.flow_sens_1);
    cJSON_AddNumberToObject(root, "flow_sensor_2", g_state.flow_sens_2);
    cJSON_AddNumberToObject(root, "moisture_1", g_state.moisture_1);
    cJSON_AddNumberToObject(root, "moisture_2", g_state.moisture_2);
    cJSON_AddNumberToObject(root, "moisture_3", g_state.moisture_3);
    cJSON_AddNumberToObject(root, "moisture_4", g_state.moisture_4);

    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/pump
// Body: {"pump": 1, "state": "on"} or {"pump": 2, "state": "off"}
// ─────────────────────────────────────────────

esp_err_t pump_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char body[64];
    if (read_body(req, body, sizeof(body)) != ESP_OK)
    {
        send_error(req, "Body too large or read failed");
        return ESP_FAIL;
    }

    cJSON *json = cJSON_Parse(body);
    if (json == NULL)
    {
        send_error(req, "Invalid JSON");
        return ESP_FAIL;
    }

    cJSON *j_pump  = cJSON_GetObjectItem(json, "pump");
    cJSON *j_state = cJSON_GetObjectItem(json, "state");

    if (!cJSON_IsNumber(j_pump) || !cJSON_IsString(j_state))
    {
        cJSON_Delete(json);
        send_error(req, "Missing or invalid fields: pump (int), state (string)");
        return ESP_FAIL;
    }

    int pump_num       = j_pump->valueint;
    const char *state  = j_state->valuestring;
    bool turn_on       = (strcmp(state, "on") == 0);

    if (pump_num != 1 && pump_num != 2)
    {
        cJSON_Delete(json);
        send_error(req, "pump must be 1 or 2");
        return ESP_FAIL;
    }

    if (turn_on)
        pump_on((PUMP)pump_num);
    else
        pump_off((PUMP)pump_num);

    cJSON_Delete(json);

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddNumberToObject(root, "pump", pump_num);
    cJSON_AddStringToObject(root, "state", turn_on ? "on" : "off");
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/led
// Body: {"state": "on"} or {"state": "off"}
// ─────────────────────────────────────────────

esp_err_t led_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char body[32];
    if (read_body(req, body, sizeof(body)) != ESP_OK)
    {
        send_error(req, "Body too large or read failed");
        return ESP_FAIL;
    }

    cJSON *json = cJSON_Parse(body);
    if (json == NULL)
    {
        send_error(req, "Invalid JSON");
        return ESP_FAIL;
    }

    cJSON *j_state = cJSON_GetObjectItem(json, "state");
    if (!cJSON_IsString(j_state))
    {
        cJSON_Delete(json);
        send_error(req, "Missing field: state (string)");
        return ESP_FAIL;
    }

    bool turn_on = (strcmp(j_state->valuestring, "on") == 0);
    cJSON_Delete(json);

    if (turn_on)
        led_on();
    else
        led_off();

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "state", turn_on ? "on" : "off");
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/profile
// Creates or replaces the active profile.
//
// Expected JSON body:
// {
//   "profile_name": "My Garden",
//   "plant_name": "Tomato",
//   "irrig_times_per_day": 2,
//   "times_of_day": [480, 1080],   <- minutes since midnight (0-1439)
//   "water_amount_per_irrig": 500, <- milliliters
//   "moisture_threshold": 30       <- percent (0-100); irrigate only if below this
// }
// ─────────────────────────────────────────────

esp_err_t profile_create_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char body[512];
    if (read_body(req, body, sizeof(body)) != ESP_OK)
    {
        send_error(req, "Body too large or read failed");
        return ESP_FAIL;
    }

    cJSON *json = cJSON_Parse(body);
    if (json == NULL)
    {
        send_error(req, "Invalid JSON");
        return ESP_FAIL;
    }

    cJSON *j_pname    = cJSON_GetObjectItem(json, "profile_name");
    cJSON *j_plant    = cJSON_GetObjectItem(json, "plant_name");
    cJSON *j_times    = cJSON_GetObjectItem(json, "irrig_times_per_day");
    cJSON *j_tod      = cJSON_GetObjectItem(json, "times_of_day");
    cJSON *j_amount   = cJSON_GetObjectItem(json, "water_amount_per_irrig");
    cJSON *j_moisture = cJSON_GetObjectItem(json, "moisture_threshold");

    if (!cJSON_IsString(j_pname)  ||
        !cJSON_IsString(j_plant)  ||
        !cJSON_IsNumber(j_times)  ||
        !cJSON_IsArray(j_tod)     ||
        !cJSON_IsNumber(j_amount) ||
        !cJSON_IsNumber(j_moisture))
    {
        cJSON_Delete(json);
        send_error(req, "Missing or invalid fields");
        return ESP_FAIL;
    }

    int irrig_times = j_times->valueint;
    if (irrig_times < 1 || irrig_times > MAX_IRRIG_TIME_PER_DAY)
    {
        cJSON_Delete(json);
        send_error(req, "irrig_times_per_day out of range (1-24)");
        return ESP_FAIL;
    }

    if (cJSON_GetArraySize(j_tod) != irrig_times)
    {
        cJSON_Delete(json);
        send_error(req, "times_of_day array length must match irrig_times_per_day");
        return ESP_FAIL;
    }

    // Copy everything into s_profile
    memset(&s_profile, 0, sizeof(s_profile));
    strncpy(s_profile.profile_name, j_pname->valuestring, MAX_NAME_LENGTH - 1);
    strncpy(s_profile.plant_name,   j_plant->valuestring, MAX_NAME_LENGTH - 1);
    s_profile.irrig_times_per_day    = (uint8_t)irrig_times;
    s_profile.water_amount_per_irrig = (uint16_t)j_amount->valueint;
    s_profile.moisture_threshold     = (uint8_t)j_moisture->valueint;

    for (int i = 0; i < irrig_times; i++)
    {
        cJSON *item = cJSON_GetArrayItem(j_tod, i);
        s_profile.times_of_day[i] = (uint16_t)item->valueint; // minutes since midnight
    }

    s_profile_active = true;
    cJSON_Delete(json);

    ESP_LOGI(WIFI_TAG, "Profile created: %s / %s", s_profile.profile_name, s_profile.plant_name);

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "msg", "Profile saved");
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// GET /api/profile
// Returns the active profile, or 404 if none exists.
// ─────────────────────────────────────────────

esp_err_t profile_read_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    if (!s_profile_active)
    {
        send_error(req, "No active profile");
        return ESP_OK;
    }

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "profile_name", s_profile.profile_name);
    cJSON_AddStringToObject(root, "plant_name",   s_profile.plant_name);
    cJSON_AddNumberToObject(root, "irrig_times_per_day",    s_profile.irrig_times_per_day);
    cJSON_AddNumberToObject(root, "water_amount_per_irrig", s_profile.water_amount_per_irrig);
    cJSON_AddNumberToObject(root, "moisture_threshold",     s_profile.moisture_threshold);

    cJSON *tod_arr = cJSON_CreateArray();
    for (int i = 0; i < s_profile.irrig_times_per_day; i++)
    {
        cJSON_AddItemToArray(tod_arr, cJSON_CreateNumber(s_profile.times_of_day[i]));
    }
    cJSON_AddItemToObject(root, "times_of_day", tod_arr);

    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// DELETE /api/profile
// Clears the active profile.
// ─────────────────────────────────────────────

esp_err_t profile_delete_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    memset(&s_profile, 0, sizeof(s_profile));
    s_profile_active = false;
    ESP_LOGI(WIFI_TAG, "Profile deleted");

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "msg", "Profile deleted");
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// WIFI EVENT HANDLER
// ─────────────────────────────────────────────

static void wifi_event_handler(void *arg, esp_event_base_t base,
                                int32_t event_id, void *event_data)
{
    if (event_id == WIFI_EVENT_AP_STACONNECTED)
    {
        wifi_event_ap_staconnected_t *e = (wifi_event_ap_staconnected_t *)event_data;
        ESP_LOGI(WIFI_TAG, "Client connected, AID=%d", e->aid);
    }
    else if (event_id == WIFI_EVENT_AP_STADISCONNECTED)
    {
        wifi_event_ap_stadisconnected_t *e = (wifi_event_ap_stadisconnected_t *)event_data;
        ESP_LOGI(WIFI_TAG, "Client disconnected, AID=%d", e->aid);
    }
}

// ─────────────────────────────────────────────
// SERVER STARTUP
// ─────────────────────────────────────────────

static httpd_handle_t start_webserver(void)
{
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();
    config.server_port = 80;
    config.max_uri_handlers = 16;

    httpd_handle_t server = NULL;
    if (httpd_start(&server, &config) != ESP_OK)
    {
        ESP_LOGE(WIFI_TAG, "Failed to start HTTP server");
        return NULL;
    }

    // Preflight (OPTIONS) — must be registered before specific routes
    httpd_uri_t uri_options = {
        .uri = "/api/*", .method = HTTP_OPTIONS, .handler = options_handler
    };

    // GET /api/status — all sensor data and device states
    httpd_uri_t uri_status = {
        .uri = "/api/status", .method = HTTP_GET, .handler = status_handler
    };

    // POST /api/pump — turn a pump on or off
    httpd_uri_t uri_pump = {
        .uri = "/api/pump", .method = HTTP_POST, .handler = pump_handler
    };

    // POST /api/led — turn LED on or off
    httpd_uri_t uri_led = {
        .uri = "/api/led", .method = HTTP_POST, .handler = led_handler
    };

    // POST /api/profile — create or replace active profile
    httpd_uri_t uri_profile_create = {
        .uri = "/api/profile", .method = HTTP_POST, .handler = profile_create_handler
    };

    // GET /api/profile — read active profile
    httpd_uri_t uri_profile_read = {
        .uri = "/api/profile", .method = HTTP_GET, .handler = profile_read_handler
    };

    // DELETE /api/profile — delete active profile
    httpd_uri_t uri_profile_delete = {
        .uri = "/api/profile", .method = HTTP_DELETE, .handler = profile_delete_handler
    };

    httpd_register_uri_handler(server, &uri_options);
    httpd_register_uri_handler(server, &uri_status);
    httpd_register_uri_handler(server, &uri_pump);
    httpd_register_uri_handler(server, &uri_led);
    httpd_register_uri_handler(server, &uri_profile_create);
    httpd_register_uri_handler(server, &uri_profile_read);
    httpd_register_uri_handler(server, &uri_profile_delete);

    ESP_LOGI(WIFI_TAG, "HTTP server started on port %d", config.server_port);
    return server;
}

// ─────────────────────────────────────────────
// SOFTAP INIT
// ─────────────────────────────────────────────

static void wifi_init_softap(void)
{
    esp_netif_create_default_wifi_ap();

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));

    ESP_ERROR_CHECK(esp_event_handler_instance_register(
        WIFI_EVENT, ESP_EVENT_ANY_ID, &wifi_event_handler, NULL, NULL));

    wifi_config_t wifi_config = {
        .ap = {
            .ssid            = ESP32_SSID,
            .ssid_len        = strlen(ESP32_SSID),
            .channel         = 1,
            .password        = ESP32_PASS,
            .max_connection  = 4,
            .authmode        = WIFI_AUTH_WPA_WPA2_PSK,
        },
    };

    if (strlen(ESP32_PASS) == 0)
    {
        wifi_config.ap.authmode = WIFI_AUTH_OPEN;
    }

    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_AP));
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_AP, &wifi_config));
    ESP_ERROR_CHECK(esp_wifi_start());

    ESP_LOGI(WIFI_TAG, "SoftAP started — SSID: %s", ESP32_SSID);
}

// ─────────────────────────────────────────────
// PUBLIC ENTRY POINT
// ─────────────────────────────────────────────

void init_networking(void)
{
    ESP_ERROR_CHECK(nvs_flash_init());
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    wifi_init_softap();
    start_webserver();
    ESP_LOGI(WIFI_TAG, "Networking initialized");
    g_state.wifi_state = true;
}