#include "networking.h"

const char *TAG = "SOFTAP_HTTP";

void give_command(int val)
{
    // ESP_LOGI(TAG, "Action function called with val=%d", val);
    //  Your custom logic here
    switch (val)
    {
        case 1: led_on(); printf("LED turned on from website!\n"); break;
        case 2: led_off(); printf("LED turned off from website!\n"); break;
        case 3: led_print_state(); break;
    }
}

esp_err_t api_cmd_handler(httpd_req_t *req)
{
    // Add CORS headers
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Headers", "Content-Type");
    
    char buf[64];
    int ret = httpd_req_get_url_query_str(req, buf, sizeof(buf));

    if (ret == ESP_OK)
    {
        char param_val[8];

        if (httpd_query_key_value(buf, "val", param_val, sizeof(param_val)) == ESP_OK)
        {
            int value = atoi(param_val);
            ESP_LOGI(TAG, "Received API Command: %d", value);

            give_command(value);

            httpd_resp_sendstr(req, "OK");
            return ESP_OK;
        }
    }

    httpd_resp_send_err(req, HTTPD_400_BAD_REQUEST, "Missing or invalid parameter");
    return ESP_FAIL;
}

httpd_handle_t start_webserver(void)
{
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();
    config.server_port = 80;

    ESP_LOGI(TAG, "Starting HTTP server on port %d", config.server_port);

    httpd_handle_t server = NULL;
    if (httpd_start(&server, &config) == ESP_OK)
    {

        // Register API endpoint
        httpd_uri_t api_cmd_uri = {
            .uri = "/api/cmd",
            .method = HTTP_GET,
            .handler = api_cmd_handler,
            .user_ctx = NULL};

        httpd_register_uri_handler(server, &api_cmd_uri);

        return server;
    }

    ESP_LOGI(TAG, "Failed to start HTTP server");
    return NULL;
}

void wifi_init_softap(void)
{
    esp_netif_create_default_wifi_ap();

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));

    wifi_config_t wifi_config = {
        .ap = {
            .ssid = "ESP32_AP",
            .ssid_len = strlen("ESP32_AP"),
            .channel = 1,
            .password = "12345678",
            .max_connection = 4,
            .authmode = WIFI_AUTH_WPA_WPA2_PSK,
        },
    };

    if (strlen("12345678") == 0)
    {
        wifi_config.ap.authmode = WIFI_AUTH_OPEN;
    }

    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_AP));
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_AP, &wifi_config));
    ESP_ERROR_CHECK(esp_wifi_start());

    ESP_LOGI(TAG, "SoftAP created. SSID: ESP32_AP Password: 12345678");
}

void init_networking(void)
{
    // Initialize NVS
    ESP_ERROR_CHECK(nvs_flash_init());
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());

    wifi_init_softap();
    
    start_webserver();
}
