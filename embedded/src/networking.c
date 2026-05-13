#include "networking.h"

// ─────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────
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

static esp_err_t read_body(httpd_req_t *req, char *buf, size_t buf_size)
{
    if (req->content_len >= buf_size)
    {
        ESP_LOGI(WIFI_TAG, "POST body too large: %d bytes", req->content_len);
        return ESP_FAIL;
    }

    int received = httpd_req_recv(req, buf, req->content_len);
    if (received <= 0)
        return ESP_FAIL;

    buf[received] = '\0';
    return ESP_OK;
}

// ─────────────────────────────────────────────
// OPTIONS — CORS preflight
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
// ─────────────────────────────────────────────
esp_err_t status_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status",          "ok");
    cJSON_AddBoolToObject(root,   "led",              g_state.led_state);
    cJSON_AddBoolToObject(root,   "pump_1",           g_state.pump_1_state);
    cJSON_AddBoolToObject(root,   "pump_2",           g_state.pump_2_state);
    cJSON_AddBoolToObject(root,   "pump_1_triggered", g_state.pump_1_triggered);
    cJSON_AddBoolToObject(root,   "pump_2_triggered", g_state.pump_2_triggered);
    cJSON_AddBoolToObject(root,   "tank",             g_state.tank_state);
    cJSON_AddBoolToObject(root,   "pipe",             g_state.pipe_state);
    cJSON_AddNumberToObject(root, "temperature",      g_state.temperature);
    cJSON_AddNumberToObject(root, "humidity",         g_state.humidity);
    cJSON_AddNumberToObject(root, "light_intensity",  g_state.light_intensity);
    cJSON_AddNumberToObject(root, "flow_sensor_1",    g_state.flow_sens_1);
    cJSON_AddNumberToObject(root, "flow_sensor_2",    g_state.flow_sens_2);
    cJSON_AddNumberToObject(root, "moisture_1",       g_state.moisture_1);
    cJSON_AddNumberToObject(root, "moisture_2",       g_state.moisture_2);
    cJSON_AddNumberToObject(root, "moisture_3",       g_state.moisture_3);
    cJSON_AddNumberToObject(root, "moisture_4",       g_state.moisture_4);

    // Embed current profile as nested object
    cJSON *profile = cJSON_CreateObject();
    cJSON_AddNumberToObject(profile, "moist_upper",      g_state.profile.moist_upper);
    cJSON_AddNumberToObject(profile, "moist_lower",      g_state.profile.moist_lower);
    cJSON_AddNumberToObject(profile, "temp_upper",       g_state.profile.temp_upper);
    cJSON_AddNumberToObject(profile, "temp_lower",       g_state.profile.temp_lower);
    cJSON_AddNumberToObject(profile, "hum_upper",        g_state.profile.hum_upper);
    cJSON_AddNumberToObject(profile, "hum_lower",        g_state.profile.hum_lower);
    cJSON_AddNumberToObject(profile, "light_threshold",  g_state.profile.light_threshold);
    cJSON_AddItemToObject(root, "profile", profile);

    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// GET /api/profile
// ─────────────────────────────────────────────
esp_err_t profile_get_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status",          "ok");
    cJSON_AddNumberToObject(root, "moist_upper",     g_state.profile.moist_upper);
    cJSON_AddNumberToObject(root, "moist_lower",     g_state.profile.moist_lower);
    cJSON_AddNumberToObject(root, "temp_upper",      g_state.profile.temp_upper);
    cJSON_AddNumberToObject(root, "temp_lower",      g_state.profile.temp_lower);
    cJSON_AddNumberToObject(root, "hum_upper",       g_state.profile.hum_upper);
    cJSON_AddNumberToObject(root, "hum_lower",       g_state.profile.hum_lower);
    cJSON_AddNumberToObject(root, "light_threshold", g_state.profile.light_threshold);

    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/profile
// All fields optional — only provided fields are updated.
// Body example:
// {
//   "moist_upper": 80, "moist_lower": 20,
//   "temp_upper": 40,  "temp_lower": 5,
//   "hum_upper": 60,   "hum_lower": 20,
//   "light_threshold": 85
// }
// ─────────────────────────────────────────────
esp_err_t profile_post_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char body[128];
    if (read_body(req, body, sizeof(body)) != ESP_OK)
    {
        send_error(req, "Body too large or read failed");
        return ESP_FAIL;
    }

    cJSON *json = cJSON_Parse(body);
    if (!json) { send_error(req, "Invalid JSON"); return ESP_FAIL; }

    // Snapshot the profile before changes so we can log only what changed
    profile_t old = g_state.profile;

    // Helper macro: update field only if present and is a number
    #define UPDATE_U8(field, key) \
        do { \
            cJSON *_j = cJSON_GetObjectItem(json, key); \
            if (cJSON_IsNumber(_j)) \
                g_state.profile.field = (uint8_t)_j->valueint; \
        } while (0)

    UPDATE_U8(moist_upper,     "moist_upper");
    UPDATE_U8(moist_lower,     "moist_lower");
    UPDATE_U8(temp_upper,      "temp_upper");
    UPDATE_U8(temp_lower,      "temp_lower");
    UPDATE_U8(hum_upper,       "hum_upper");
    UPDATE_U8(hum_lower,       "hum_lower");
    UPDATE_U8(light_threshold, "light_threshold");

    #undef UPDATE_U8

    cJSON_Delete(json);

    // ── Log what changed, then print the full current profile ────────
    ESP_LOGI(PROFILE_TAG, "Profile updated via HTTP");
    if (old.moist_upper     != g_state.profile.moist_upper)
        ESP_LOGI(PROFILE_TAG, "  moist_upper     : %d -> %d", old.moist_upper,     g_state.profile.moist_upper);
    if (old.moist_lower     != g_state.profile.moist_lower)
        ESP_LOGI(PROFILE_TAG, "  moist_lower     : %d -> %d", old.moist_lower,     g_state.profile.moist_lower);
    if (old.temp_upper      != g_state.profile.temp_upper)
        ESP_LOGI(PROFILE_TAG, "  temp_upper      : %d -> %d", old.temp_upper,      g_state.profile.temp_upper);
    if (old.temp_lower      != g_state.profile.temp_lower)
        ESP_LOGI(PROFILE_TAG, "  temp_lower      : %d -> %d", old.temp_lower,      g_state.profile.temp_lower);
    if (old.hum_upper       != g_state.profile.hum_upper)
        ESP_LOGI(PROFILE_TAG, "  hum_upper       : %d -> %d", old.hum_upper,       g_state.profile.hum_upper);
    if (old.hum_lower       != g_state.profile.hum_lower)
        ESP_LOGI(PROFILE_TAG, "  hum_lower       : %d -> %d", old.hum_lower,       g_state.profile.hum_lower);
    if (old.light_threshold != g_state.profile.light_threshold)
        ESP_LOGI(PROFILE_TAG, "  light_threshold : %d -> %d", old.light_threshold, g_state.profile.light_threshold);
    ESP_LOGI(PROFILE_TAG, "Current profile:");
    ESP_LOGI(PROFILE_TAG, "  moist_upper     = %d", g_state.profile.moist_upper);
    ESP_LOGI(PROFILE_TAG, "  moist_lower     = %d", g_state.profile.moist_lower);
    ESP_LOGI(PROFILE_TAG, "  temp_upper      = %d", g_state.profile.temp_upper);
    ESP_LOGI(PROFILE_TAG, "  temp_lower      = %d", g_state.profile.temp_lower);
    ESP_LOGI(PROFILE_TAG, "  hum_upper       = %d", g_state.profile.hum_upper);
    ESP_LOGI(PROFILE_TAG, "  hum_lower       = %d", g_state.profile.hum_lower);
    ESP_LOGI(PROFILE_TAG, "  light_threshold = %d", g_state.profile.light_threshold);

    // Echo back the full updated profile
    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status",          "ok");
    cJSON_AddNumberToObject(root, "moist_upper",     g_state.profile.moist_upper);
    cJSON_AddNumberToObject(root, "moist_lower",     g_state.profile.moist_lower);
    cJSON_AddNumberToObject(root, "temp_upper",      g_state.profile.temp_upper);
    cJSON_AddNumberToObject(root, "temp_lower",      g_state.profile.temp_lower);
    cJSON_AddNumberToObject(root, "hum_upper",       g_state.profile.hum_upper);
    cJSON_AddNumberToObject(root, "hum_lower",       g_state.profile.hum_lower);
    cJSON_AddNumberToObject(root, "light_threshold", g_state.profile.light_threshold);
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/pump
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
    if (!json) { send_error(req, "Invalid JSON"); return ESP_FAIL; }

    cJSON *j_pump   = cJSON_GetObjectItem(json, "pump");
    cJSON *j_source = cJSON_GetObjectItem(json, "source");
    cJSON *j_state  = cJSON_GetObjectItem(json, "state");

    int pump_num = 0;

    if (cJSON_IsNumber(j_pump))
    {
        pump_num = j_pump->valueint;
    }
    else if (cJSON_IsString(j_source))
    {
        if      (strcmp(j_source->valuestring, "web")    == 0) pump_num = 1;
        else if (strcmp(j_source->valuestring, "mobile") == 0) pump_num = 2;
        else
        {
            cJSON_Delete(json);
            send_error(req, "Unknown source, use 'web' or 'mobile'");
            return ESP_FAIL;
        }
    }
    else
    {
        cJSON_Delete(json);
        send_error(req, "Provide either 'pump' (int) or 'source' (string)");
        return ESP_FAIL;
    }

    if (pump_num != 1 && pump_num != 2)
    {
        cJSON_Delete(json);
        send_error(req, "pump must be 1 or 2");
        return ESP_FAIL;
    }

    if (!cJSON_IsString(j_state))
    {
        cJSON_Delete(json);
        send_error(req, "Missing field: state (string)");
        return ESP_FAIL;
    }

    bool turn_on = (strcmp(j_state->valuestring, "on") == 0);
    cJSON_Delete(json);

    PUMP pump = (pump_num == 1) ? PUMP_1 : PUMP_2;
    if (turn_on) pump_on(pump);
    else         pump_off(pump);

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddNumberToObject(root, "pump",   pump_num);
    cJSON_AddStringToObject(root, "state",  turn_on ? "on" : "off");
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/led
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
    if (!json) { send_error(req, "Invalid JSON"); return ESP_FAIL; }

    cJSON *j_state = cJSON_GetObjectItem(json, "state");
    if (!cJSON_IsString(j_state))
    {
        cJSON_Delete(json);
        send_error(req, "Missing field: state (string)");
        return ESP_FAIL;
    }

    bool turn_on = (strcmp(j_state->valuestring, "on") == 0);
    cJSON_Delete(json);

    if (turn_on) led_on();
    else         led_off();

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "state",  turn_on ? "on" : "off");
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
    config.server_port      = 80;
    config.max_uri_handlers = 20;
    config.stack_size       = 8192;

    httpd_handle_t server = NULL;
    if (httpd_start(&server, &config) != ESP_OK)
    {
        ESP_LOGE(WIFI_TAG, "Failed to start HTTP server");
        return NULL;
    }

    // OPTIONS preflight
    httpd_uri_t uri_opt_status  = { .uri = "/api/status",  .method = HTTP_OPTIONS, .handler = options_handler };
    httpd_uri_t uri_opt_pump    = { .uri = "/api/pump",    .method = HTTP_OPTIONS, .handler = options_handler };
    httpd_uri_t uri_opt_led     = { .uri = "/api/led",     .method = HTTP_OPTIONS, .handler = options_handler };
    httpd_uri_t uri_opt_profile = { .uri = "/api/profile", .method = HTTP_OPTIONS, .handler = options_handler };

    // Routes
    httpd_uri_t uri_status       = { .uri = "/api/status",  .method = HTTP_GET,  .handler = status_handler };
    httpd_uri_t uri_pump         = { .uri = "/api/pump",    .method = HTTP_POST, .handler = pump_handler };
    httpd_uri_t uri_led          = { .uri = "/api/led",     .method = HTTP_POST, .handler = led_handler };
    httpd_uri_t uri_profile_get  = { .uri = "/api/profile", .method = HTTP_GET,  .handler = profile_get_handler };
    httpd_uri_t uri_profile_post = { .uri = "/api/profile", .method = HTTP_POST, .handler = profile_post_handler };

    httpd_register_uri_handler(server, &uri_opt_status);
    httpd_register_uri_handler(server, &uri_opt_pump);
    httpd_register_uri_handler(server, &uri_opt_led);
    httpd_register_uri_handler(server, &uri_opt_profile);
    httpd_register_uri_handler(server, &uri_status);
    httpd_register_uri_handler(server, &uri_pump);
    httpd_register_uri_handler(server, &uri_led);
    httpd_register_uri_handler(server, &uri_profile_get);
    httpd_register_uri_handler(server, &uri_profile_post);

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
            .ssid           = ESP32_SSID,
            .ssid_len       = strlen(ESP32_SSID),
            .channel        = 1,
            .password       = ESP32_PASS,
            .max_connection = 4,
            .authmode       = WIFI_AUTH_WPA_WPA2_PSK,
        },
    };

    if (strlen(ESP32_PASS) == 0)
        wifi_config.ap.authmode = WIFI_AUTH_OPEN;

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
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    wifi_init_softap();
    delay_ms(100);
    start_webserver();
    ESP_LOGI(WIFI_TAG, "Networking initialized");
    g_state.wifi_state = true;
}