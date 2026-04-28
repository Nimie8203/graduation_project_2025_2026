#include "networking.h"
#include "profile.h"

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
// OPTIONS — CORS preflight for all routes
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
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddBoolToObject(root, "led",            g_state.led_state);
    cJSON_AddBoolToObject(root, "pump_1",         g_state.pump_1_state);
    cJSON_AddBoolToObject(root, "pump_2",         g_state.pump_2_state);
    cJSON_AddNumberToObject(root, "temperature",  g_state.temperature);
    cJSON_AddNumberToObject(root, "humidity",     g_state.humidity);
    cJSON_AddNumberToObject(root, "light_intensity", g_state.light_intensity);
    cJSON_AddNumberToObject(root, "flow_sensor_1",   g_state.flow_sens_1);
    cJSON_AddNumberToObject(root, "flow_sensor_2",   g_state.flow_sens_2);
    cJSON_AddNumberToObject(root, "moisture_1",  g_state.moisture_1);
    cJSON_AddNumberToObject(root, "moisture_2",  g_state.moisture_2);
    cJSON_AddNumberToObject(root, "moisture_3",  g_state.moisture_3);
    cJSON_AddNumberToObject(root, "moisture_4",  g_state.moisture_4);

    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/pump
// Body: {"pump": 1, "state": "on"}
//       {"source": "web"|"mobile", "state": "on"|"off"}
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
    cJSON_AddNumberToObject(root, "pump",  pump_num);
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
    cJSON_AddStringToObject(root, "state", turn_on ? "on" : "off");
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// GET /api/profiles
// Returns all stored profiles as a JSON array.
// ─────────────────────────────────────────────

esp_err_t profiles_list_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    profile_t profiles[MAX_PROFILES];
    uint8_t count = 0;
    esp_err_t err = load_all_profiles(profiles, &count);

    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND)
    {
        send_error(req, "Failed to read profiles from storage");
        return ESP_FAIL;
    }

    cJSON *root  = cJSON_CreateObject();
    cJSON *array = cJSON_CreateArray();

    for (uint8_t i = 0; i < count; i++)
    {
        cJSON *p = cJSON_CreateObject();
        cJSON_AddNumberToObject(p, "id",                     profiles[i].id);
        cJSON_AddStringToObject(p, "profile_name",           profiles[i].profile_name);
        cJSON_AddStringToObject(p, "plant_name",             profiles[i].plant_name);
        cJSON_AddNumberToObject(p, "irrig_times_per_day",    profiles[i].irrig_times_per_day);
        cJSON_AddNumberToObject(p, "water_amount_per_irrig", profiles[i].water_amount_per_irrig);
        cJSON_AddNumberToObject(p, "moisture_threshold",     profiles[i].moisture_threshold);

        cJSON *tod = cJSON_CreateArray();
        for (int j = 0; j < profiles[i].irrig_times_per_day; j++)
            cJSON_AddItemToArray(tod, cJSON_CreateNumber(profiles[i].times_of_day[j]));
        cJSON_AddItemToObject(p, "times_of_day", tod);

        cJSON_AddItemToArray(array, p);
    }

    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddNumberToObject(root, "count", count);
    cJSON_AddItemToObject(root, "profiles", array);
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/profiles
// Creates a new profile.
//
// Body: {
//   "id": 1,
//   "profile_name": "My Garden",
//   "plant_name": "Tomato",
//   "irrig_times_per_day": 2,
//   "times_of_day": [480, 1080],
//   "water_amount_per_irrig": 500,
//   "moisture_threshold": 30
// }
// ─────────────────────────────────────────────

esp_err_t profiles_create_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char body[512];
    if (read_body(req, body, sizeof(body)) != ESP_OK)
    {
        send_error(req, "Body too large or read failed");
        return ESP_FAIL;
    }

    cJSON *json = cJSON_Parse(body);
    if (!json) { send_error(req, "Invalid JSON"); return ESP_FAIL; }

    cJSON *j_id       = cJSON_GetObjectItem(json, "id");
    cJSON *j_pname    = cJSON_GetObjectItem(json, "profile_name");
    cJSON *j_plant    = cJSON_GetObjectItem(json, "plant_name");
    cJSON *j_times    = cJSON_GetObjectItem(json, "irrig_times_per_day");
    cJSON *j_tod      = cJSON_GetObjectItem(json, "times_of_day");
    cJSON *j_amount   = cJSON_GetObjectItem(json, "water_amount_per_irrig");
    cJSON *j_moisture = cJSON_GetObjectItem(json, "moisture_threshold");

    if (!cJSON_IsNumber(j_id)     ||
        !cJSON_IsString(j_pname)  ||
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
        send_error(req, "times_of_day length must match irrig_times_per_day");
        return ESP_FAIL;
    }

    profile_t p = {0};
    p.id = (uint8_t)j_id->valueint;
    strncpy(p.profile_name, j_pname->valuestring, MAX_NAME_LENGTH - 1);
    strncpy(p.plant_name,   j_plant->valuestring, MAX_NAME_LENGTH - 1);
    p.irrig_times_per_day    = (uint8_t)irrig_times;
    p.water_amount_per_irrig = (uint16_t)j_amount->valueint;
    p.moisture_threshold     = (uint8_t)j_moisture->valueint;

    for (int i = 0; i < irrig_times; i++)
        p.times_of_day[i] = (uint16_t)cJSON_GetArrayItem(j_tod, i)->valueint;

    cJSON_Delete(json);

    esp_err_t err = add_profile(p);
    if (err == ESP_ERR_NO_MEM)      { send_error(req, "Max profiles reached");      return ESP_FAIL; }
    if (err == ESP_ERR_INVALID_ARG) { send_error(req, "Profile ID already exists"); return ESP_FAIL; }
    if (err != ESP_OK)              { send_error(req, "Failed to save profile");     return ESP_FAIL; }

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "msg", "Profile created");
    cJSON_AddNumberToObject(root, "id", p.id);
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// GET /api/profiles/by-id?id=3
// Returns a single profile by ID.
// ─────────────────────────────────────────────

esp_err_t profiles_read_one_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char query[16];
    if (httpd_req_get_url_query_str(req, query, sizeof(query)) != ESP_OK)
    {
        send_error(req, "Missing query string (expected ?id=N)");
        return ESP_FAIL;
    }
    char id_str[8];
    if (httpd_query_key_value(query, "id", id_str, sizeof(id_str)) != ESP_OK)
    {
        send_error(req, "Missing 'id' query parameter");
        return ESP_FAIL;
    }

    uint8_t id = (uint8_t)atoi(id_str);
    profile_t p = {0};
    esp_err_t err = load_profile(id, &p);

    if (err == ESP_ERR_NVS_NOT_FOUND) { send_error(req, "Profile not found");      return ESP_OK; }
    if (err != ESP_OK)                { send_error(req, "Failed to read profile");  return ESP_FAIL; }

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddNumberToObject(root, "id",                     p.id);
    cJSON_AddStringToObject(root, "profile_name",           p.profile_name);
    cJSON_AddStringToObject(root, "plant_name",             p.plant_name);
    cJSON_AddNumberToObject(root, "irrig_times_per_day",    p.irrig_times_per_day);
    cJSON_AddNumberToObject(root, "water_amount_per_irrig", p.water_amount_per_irrig);
    cJSON_AddNumberToObject(root, "moisture_threshold",     p.moisture_threshold);

    cJSON *tod = cJSON_CreateArray();
    for (int i = 0; i < p.irrig_times_per_day; i++)
        cJSON_AddItemToArray(tod, cJSON_CreateNumber(p.times_of_day[i]));
    cJSON_AddItemToObject(root, "times_of_day", tod);

    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// POST /api/profiles/by-id?id=3
// Edits an existing profile (partial updates supported).
// ─────────────────────────────────────────────

esp_err_t profiles_edit_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char query[16];
    if (httpd_req_get_url_query_str(req, query, sizeof(query)) != ESP_OK)
    {
        send_error(req, "Missing query string (expected ?id=N)");
        return ESP_FAIL;
    }
    char id_str[8];
    if (httpd_query_key_value(query, "id", id_str, sizeof(id_str)) != ESP_OK)
    {
        send_error(req, "Missing 'id' query parameter");
        return ESP_FAIL;
    }
    uint8_t id = (uint8_t)atoi(id_str);

    char body[512];
    if (read_body(req, body, sizeof(body)) != ESP_OK)
    {
        send_error(req, "Body too large or read failed");
        return ESP_FAIL;
    }

    cJSON *json = cJSON_Parse(body);
    if (!json) { send_error(req, "Invalid JSON"); return ESP_FAIL; }

    // Load existing profile so unspecified fields keep their current values
    profile_t p = {0};
    load_profile(id, &p); // edit_profile will catch not-found below

    cJSON *j_pname    = cJSON_GetObjectItem(json, "profile_name");
    cJSON *j_plant    = cJSON_GetObjectItem(json, "plant_name");
    cJSON *j_times    = cJSON_GetObjectItem(json, "irrig_times_per_day");
    cJSON *j_tod      = cJSON_GetObjectItem(json, "times_of_day");
    cJSON *j_amount   = cJSON_GetObjectItem(json, "water_amount_per_irrig");
    cJSON *j_moisture = cJSON_GetObjectItem(json, "moisture_threshold");

    if (cJSON_IsString(j_pname))    strncpy(p.profile_name, j_pname->valuestring, MAX_NAME_LENGTH - 1);
    if (cJSON_IsString(j_plant))    strncpy(p.plant_name,   j_plant->valuestring, MAX_NAME_LENGTH - 1);
    if (cJSON_IsNumber(j_amount))   p.water_amount_per_irrig = (uint16_t)j_amount->valueint;
    if (cJSON_IsNumber(j_moisture)) p.moisture_threshold     = (uint8_t)j_moisture->valueint;

    if (cJSON_IsNumber(j_times) && cJSON_IsArray(j_tod))
    {
        int irrig_times = j_times->valueint;
        if (irrig_times < 1 || irrig_times > MAX_IRRIG_TIME_PER_DAY ||
            cJSON_GetArraySize(j_tod) != irrig_times)
        {
            cJSON_Delete(json);
            send_error(req, "Invalid irrig_times_per_day or times_of_day");
            return ESP_FAIL;
        }
        p.irrig_times_per_day = (uint8_t)irrig_times;
        memset(p.times_of_day, 0, sizeof(p.times_of_day));
        for (int i = 0; i < irrig_times; i++)
            p.times_of_day[i] = (uint16_t)cJSON_GetArrayItem(j_tod, i)->valueint;
    }

    p.id = id;
    cJSON_Delete(json);

    esp_err_t err = edit_profile(p);
    if (err == ESP_ERR_NOT_FOUND) { send_error(req, "Profile not found");       return ESP_FAIL; }
    if (err != ESP_OK)            { send_error(req, "Failed to update profile"); return ESP_FAIL; }

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "msg", "Profile updated");
    cJSON_AddNumberToObject(root, "id", id);
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// DELETE /api/profiles/by-id?id=3
// Deletes one profile by ID.
// ─────────────────────────────────────────────

esp_err_t profiles_delete_one_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    char query[16];
    if (httpd_req_get_url_query_str(req, query, sizeof(query)) != ESP_OK)
    {
        send_error(req, "Missing query string (expected ?id=N)");
        return ESP_FAIL;
    }
    char id_str[8];
    if (httpd_query_key_value(query, "id", id_str, sizeof(id_str)) != ESP_OK)
    {
        send_error(req, "Missing 'id' query parameter");
        return ESP_FAIL;
    }

    uint8_t id = (uint8_t)atoi(id_str);
    esp_err_t err = remove_profile(id);

    if (err == ESP_ERR_NOT_FOUND) { send_error(req, "Profile not found");       return ESP_OK; }
    if (err != ESP_OK)            { send_error(req, "Failed to delete profile"); return ESP_FAIL; }

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "msg", "Profile deleted");
    cJSON_AddNumberToObject(root, "id", id);
    send_json(req, root);
    return ESP_OK;
}

// ─────────────────────────────────────────────
// DELETE /api/profiles
// Wipes all profiles.
// ─────────────────────────────────────────────

esp_err_t profiles_delete_all_handler(httpd_req_t *req)
{
    add_cors_headers(req);

    esp_err_t err = remove_all_profiles();
    if (err != ESP_OK) { send_error(req, "Failed to clear profiles"); return ESP_FAIL; }

    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddStringToObject(root, "msg", "All profiles deleted");
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
    config.server_port     = 80;
    config.max_uri_handlers = 20; // exactly covers the 16 handlers below, +4 headroom
    config.stack_size      = 8192;

    httpd_handle_t server = NULL;
    if (httpd_start(&server, &config) != ESP_OK)
    {
        ESP_LOGE(WIFI_TAG, "Failed to start HTTP server");
        return NULL;
    }

    // ── OPTIONS (CORS preflight) ──────────────────────────
    httpd_uri_t uri_opt_status     = { .uri = "/api/status",           .method = HTTP_OPTIONS, .handler = options_handler };
    httpd_uri_t uri_opt_pump       = { .uri = "/api/pump",             .method = HTTP_OPTIONS, .handler = options_handler };
    httpd_uri_t uri_opt_led        = { .uri = "/api/led",              .method = HTTP_OPTIONS, .handler = options_handler };
    httpd_uri_t uri_opt_profiles   = { .uri = "/api/profiles",         .method = HTTP_OPTIONS, .handler = options_handler };
    httpd_uri_t uri_opt_prof_byid  = { .uri = "/api/profiles/by-id",   .method = HTTP_OPTIONS, .handler = options_handler };

    // ── Status ────────────────────────────────────────────
    httpd_uri_t uri_status         = { .uri = "/api/status",           .method = HTTP_GET,    .handler = status_handler };

    // ── Pump ──────────────────────────────────────────────
    httpd_uri_t uri_pump           = { .uri = "/api/pump",             .method = HTTP_POST,   .handler = pump_handler };

    // ── LED ───────────────────────────────────────────────
    httpd_uri_t uri_led            = { .uri = "/api/led",              .method = HTTP_POST,   .handler = led_handler };

    // ── Profiles (collection) ─────────────────────────────
    httpd_uri_t uri_profiles_list  = { .uri = "/api/profiles",         .method = HTTP_GET,    .handler = profiles_list_handler };
    httpd_uri_t uri_profiles_create= { .uri = "/api/profiles",         .method = HTTP_POST,   .handler = profiles_create_handler };
    httpd_uri_t uri_profiles_del_all={ .uri = "/api/profiles",         .method = HTTP_DELETE, .handler = profiles_delete_all_handler };

    // ── Profiles (single, by-id) ──────────────────────────
    httpd_uri_t uri_prof_read      = { .uri = "/api/profiles/by-id",   .method = HTTP_GET,    .handler = profiles_read_one_handler };
    httpd_uri_t uri_prof_edit      = { .uri = "/api/profiles/by-id",   .method = HTTP_POST,   .handler = profiles_edit_handler };
    httpd_uri_t uri_prof_del_one   = { .uri = "/api/profiles/by-id",   .method = HTTP_DELETE, .handler = profiles_delete_one_handler };

    httpd_register_uri_handler(server, &uri_opt_status);
    httpd_register_uri_handler(server, &uri_opt_pump);
    httpd_register_uri_handler(server, &uri_opt_led);
    httpd_register_uri_handler(server, &uri_opt_profiles);
    httpd_register_uri_handler(server, &uri_opt_prof_byid);
    httpd_register_uri_handler(server, &uri_status);
    httpd_register_uri_handler(server, &uri_pump);
    httpd_register_uri_handler(server, &uri_led);
    httpd_register_uri_handler(server, &uri_profiles_list);
    httpd_register_uri_handler(server, &uri_profiles_create);
    httpd_register_uri_handler(server, &uri_profiles_del_all);
    httpd_register_uri_handler(server, &uri_prof_read);
    httpd_register_uri_handler(server, &uri_prof_edit);
    httpd_register_uri_handler(server, &uri_prof_del_one);

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
    ESP_ERROR_CHECK(nvs_flash_init());
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    wifi_init_softap();
    delay_ms(100);
    start_webserver();
    ESP_LOGI(WIFI_TAG, "Networking initialized");
    g_state.wifi_state = true;
}