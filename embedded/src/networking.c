#include "networking.h"

#define COMMAND_BUFFER_SIZE 64

static void send_json(httpd_req_t *req, cJSON *root)
{
    char *json_str = cJSON_PrintUnformatted(root);
    httpd_resp_set_type(req, "application/json");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_sendstr(req, json_str);
    cJSON_free(json_str);
    cJSON_Delete(root);
}

static void add_cors(httpd_req_t *req)
{
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Headers", "Content-Type");
}

static cJSON *execute_command(int val)
{
    cJSON *root = cJSON_CreateObject();
    cJSON_AddNumberToObject(root, "cmd", val);

    switch (val)
    {
    case LED_ON:
        led_on();
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "LED turned on");
        break;

    case LED_OFF:
        led_off();
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "LED turned off");
        break;

    case LED_READ:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddNumberToObject(root, "led", g_state.led_state);
        break;

    case PUMP_1_ON:
        pump_on(1);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "Pump 1 turned on");
        break;

    case PUMP_1_OFF:
        pump_off(1);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "Pump 1 turned off");
        break;
    case PUMP_2_ON:
        pump_on(2);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "Pump 2 turned on");
        break;

    case PUMP_2_OFF:
        pump_off(2);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "Pump 2 turned off");
        break;

    case TH_READ:
        read_th(&g_state.humidity, &g_state.temperature);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddNumberToObject(root, "temperature", g_state.temperature);
        cJSON_AddNumberToObject(root, "humidity", g_state.humidity);
        break;

    case LIGHT_READ:
        // int lux = read_light();
        // cJSON_AddNumberToObject(root, "lux", lux);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "stub — wire up read_light()");
        break;

    case MOISTURE_READ:
        // int moisture = read_moisture();
        // cJSON_AddNumberToObject(root, "moisture", moisture);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "stub — wire up read_moisture()");
        break;

    case FLOW_READ:
        // float flow = read_flow();
        // cJSON_AddNumberToObject(root, "flow", flow);
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "msg", "stub — wire up read_flow()");
        break;

    case STATUS_GENERAL:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddNumberToObject(root, "led", g_state.led_state);
        cJSON_AddNumberToObject(root, "pump 1", g_state.pump_1_state);
        cJSON_AddNumberToObject(root, "pump 2", g_state.pump_2_state);
        cJSON_AddNumberToObject(root, "flow sens 1", g_state.flow_sens_1);
        cJSON_AddNumberToObject(root, "flow sens 2", g_state.flow_sens_2);
        cJSON_AddNumberToObject(root, "ligh intensity", g_state.light_intensity);
        cJSON_AddNumberToObject(root, "temperature", g_state.temperature);
        cJSON_AddNumberToObject(root, "humidity", g_state.humidity);
        break;
    case STATUS_LED:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddNumberToObject(root, "led", g_state.led_state);
        break;
    case STATUS_PUMP_1:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddNumberToObject(root, "pump", g_state.pump_1_state);
        break;
    case STATUS_PUMP_2:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddNumberToObject(root, "pump", g_state.pump_2_state);
        break;
    case PROFILE_CREATE:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "device", "ESP32 Plant Monitor");
        cJSON_AddStringToObject(root, "fw_version", "1.0.0");
        break;
    case PROFILE_READ:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "device", "ESP32 Plant Monitor");
        cJSON_AddStringToObject(root, "fw_version", "1.0.0");
        break;
    case PROFILE_DELETE:
        cJSON_AddStringToObject(root, "status", "ok");
        cJSON_AddStringToObject(root, "device", "ESP32 Plant Monitor");
        cJSON_AddStringToObject(root, "fw_version", "1.0.0");
        break;
    default:
        cJSON_AddStringToObject(root, "status", "error");
        cJSON_AddStringToObject(root, "msg", "Unknown command");
        break;
    }

    return root;
}

esp_err_t api_cmd_handler(httpd_req_t *req)
{
    add_cors(req);

    char buf[COMMAND_BUFFER_SIZE];
    if (httpd_req_get_url_query_str(req, buf, sizeof(buf)) == ESP_OK)
    {
        char param_val[8];
        if (httpd_query_key_value(buf, "val", param_val, sizeof(param_val)) == ESP_OK)
        {
            int value = atoi(param_val);
            ESP_LOGI(WIFI_TAG, "CMD: %d", value);

            cJSON *response = execute_command(value);
            send_json(req, response); // response JSON includes sensor data
            return ESP_OK;
        }
    }

    httpd_resp_send_err(req, HTTPD_400_BAD_REQUEST, "Missing ?val=N");
    return ESP_FAIL;
}

esp_err_t api_status_handler(httpd_req_t *req)
{
    add_cors(req);

    // Optionally do a fresh sensor read here, or just return cached g_state
    cJSON *root = cJSON_CreateObject();
    cJSON_AddStringToObject(root, "status", "ok");
    cJSON_AddNumberToObject(root, "led", g_state.led_state);
    cJSON_AddNumberToObject(root, "pump 1", g_state.pump_1_state);
    cJSON_AddNumberToObject(root, "pump 2", g_state.pump_2_state);
    cJSON_AddNumberToObject(root, "temperature", g_state.temperature);
    cJSON_AddNumberToObject(root, "humidity", g_state.humidity);

    send_json(req, root);
    return ESP_OK;
}

esp_err_t options_handler(httpd_req_t *req)
{
    add_cors(req);
    httpd_resp_set_status(req, "204 No Content");
    httpd_resp_send(req, NULL, 0);
    return ESP_OK;
}

httpd_handle_t start_webserver(void)
{
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();
    config.server_port = 80;

    httpd_handle_t server = NULL;
    if (httpd_start(&server, &config) != ESP_OK)
    {
        ESP_LOGE(WIFI_TAG, "Failed to start HTTP server");
        return NULL;
    }

    httpd_uri_t api_cmd = {
        .uri = "/api/cmd", .method = HTTP_GET, .handler = api_cmd_handler, .user_ctx = NULL};
    httpd_uri_t api_status = {
        .uri = "/api/status", .method = HTTP_GET, .handler = api_status_handler, .user_ctx = NULL};
    httpd_uri_t api_options = {
        .uri = "/api/*", .method = HTTP_OPTIONS, .handler = options_handler, .user_ctx = NULL};

    httpd_register_uri_handler(server, &api_cmd);
    httpd_register_uri_handler(server, &api_status);
    httpd_register_uri_handler(server, &api_options);

    ESP_LOGI(WIFI_TAG, "HTTP server started");
    return server;
}

void wifi_init_softap(void)
{
    esp_netif_create_default_wifi_ap();

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));

    wifi_config_t wifi_config = {
        .ap = {
            .ssid = ESP32_SSID,
            .ssid_len = strlen(ESP32_SSID),
            .channel = 1,
            .password = ESP32_PASS,
            .max_connection = 4,
            .authmode = WIFI_AUTH_WPA_WPA2_PSK,
        },
    };

    if (strlen(ESP32_PASS) == 0)
    {
        wifi_config.ap.authmode = WIFI_AUTH_OPEN;
    }

    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_AP));
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_AP, &wifi_config));
    ESP_ERROR_CHECK(esp_wifi_start());

    ESP_LOGI(WIFI_TAG, "SoftAP created!");
}

void init_networking(void)
{
    ESP_ERROR_CHECK(nvs_flash_init());
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    wifi_init_softap();
    start_webserver();
    g_state.wifi_state = true;
    ESP_LOGI(WIFI_TAG, "Networking Initialized");
}
