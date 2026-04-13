#ifndef NETWORKING_H
#define NETWORKING_H

#include "esp_idf_common.h"
#include "cJSON.h"

enum Commands
{
    LED_ON,
    LED_OFF,
    PUMP_1_ON,
    PUMP_1_OFF,
    PUMP_2_ON,
    PUMP_2_OFF,
    LED_READ,
    TH_READ,
    LIGHT_READ,
    MOISTURE_READ,
    FLOW_READ,
    STATUS_GENERAL,
    STATUS_LED,
    STATUS_PUMP_1,
    STATUS_PUMP_2,
    PROFILE
};

extern device_status_t g_status;

#define ESP32_SSID "ESP32_AP"
#define ESP32_PASS "82138213"

static void send_json(httpd_req_t *req, cJSON *root);
static void add_cors(httpd_req_t *req);
static cJSON *execute_command(int val);
esp_err_t api_cmd_handler(httpd_req_t *req);
esp_err_t api_status_handler(httpd_req_t *req);
esp_err_t options_handler(httpd_req_t *req);
httpd_handle_t start_webserver(void);
void wifi_init_softap(void);
void init_networking(void);

#endif