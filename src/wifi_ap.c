#include "driver/gpio.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_wifi.h"
#include "esp_log.h"
#include "esp_event.h"
#include "nvs_flash.h"
#include "esp_netif.h"
#include "esp_http_server.h"
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "blinker_v1.h"
#include "serial_input.h"

#define WIFI_SSID "ESP32-AP"
#define WIFI_PASS "password1234"

const char *TAG = "AP_SERVER";

esp_err_t api_led_on(httpd_req_t *req)
{
    // Line 1: Call your LED function
    led_on();

    // Line 2: Set the response type to JSON (data format)
    httpd_resp_set_type(req, "application/json");

    // Line 3: Allow websites from anywhere to access this (CORS)
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");

    // Line 4: Send back a response saying it worked
    httpd_resp_send(req, "{\"status\":\"on\"}", 16);

    // Line 5: Return "everything went OK"
    return ESP_OK;
}

esp_err_t api_led_off(httpd_req_t *req)
{
    led_off();
    httpd_resp_set_type(req, "application/json");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    httpd_resp_send(req, "{\"status\":\"off\"}", 17);
    return ESP_OK;
}

esp_err_t api_led_status(httpd_req_t *req)
{
    // For now, just return a dummy status
    httpd_resp_set_type(req, "application/json");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
    if (led_get_state() == 1)
    {
        httpd_resp_send(req, "{\"status\":\"led on\"}", 21);
    }
    else if (led_get_state() == 0)
    {
        httpd_resp_send(req, "{\"status\":\"led off\"}", 21);
    }
    else
    {
        httpd_resp_send(req, "{\"status\":\"unknown\"}", 21);
    }

    return ESP_OK;
}

esp_err_t api_led_blink(httpd_req_t *req)
{
    

    return ESP_OK;
}

void start_webserver(void)
{
    // Line 1: Create default configuration for the server
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();

    // Line 2: Create a variable to store the server handle
    httpd_handle_t server = NULL;

    // Line 3: Try to start the server
    if (httpd_start(&server, &config) == ESP_OK)
    {

        // Register the /api/led/on endpoint
        httpd_uri_t uri_led_on = {
            .uri = "/api/led/on", // The URL path
            .method = HTTP_GET,   // GET request
            .handler = api_led_on // Function to call
        };
        httpd_register_uri_handler(server, &uri_led_on);

        // Register the /api/led/off endpoint
        httpd_uri_t uri_led_off = {
            .uri = "/api/led/off",
            .method = HTTP_GET,
            .handler = api_led_off};
        httpd_register_uri_handler(server, &uri_led_off);

        // Register the /api/led/status endpoint
        httpd_uri_t uri_led_status = {
            .uri = "/api/led/status",
            .method = HTTP_GET,
            .handler = api_led_status};
        httpd_register_uri_handler(server, &uri_led_status);

        ESP_LOGI(TAG, "API Server started!");
    }
}

void wifi_init_ap(void)
{
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    
    esp_netif_create_default_wifi_ap();

    wifi_init_config_t config = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_wifi_init(&config));

    ESP_ERROR_CHECK(esp_event_loop_create_default());

    wifi_config_t wifi_config = {
        .ap = {
            .ssid = WIFI_SSID,
            .ssid_len = strlen(WIFI_SSID),
            .channel = 1,
            .password = WIFI_PASS,
            .max_connection = 5,
            .authmode = WIFI_AUTH_WPA_WPA2_PSK},
    };

    if (strlen((char *)wifi_config.ap.password) == 0)
    {
        wifi_config.ap.authmode = WIFI_AUTH_OPEN;
    }

    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_AP));
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_AP, &wifi_config));
    ESP_ERROR_CHECK(esp_wifi_start());

    ESP_LOGI(TAG, "Wifi hostspot started. SSID:%s password:%s",
             wifi_config.ap.ssid,
             wifi_config.ap.password);

    start_webserver();
}

void wifi_ap_start(void)
{
    printf("\nStarting WIFI Access Point...\n");
    ESP_ERROR_CHECK(nvs_flash_init());
    wifi_init_ap();
}