#ifndef NETWORKING_H
#define NETWORKING_H

#include "esp_idf_common.h"
#include "cJSON.h"

typedef struct
{
    int16_t temperature;
    int16_t humidity;
    int led_state;
    int pump_state;
} device_status_t;

enum Commands
{
    LED_ON,
    LED_OFF,
    PUMP_ON,
    PUMP_OFF,
    LED_READ,
    TH_READ,
    LIGHT_READ,
    MOISTURE_READ,
    FLOW_READ,
    STATUS_GENERAL,
    STATUS_LED,
    STATUS_PUMP,
    PROFILE
};

extern device_status_t g_status;

esp_err_t api_cmd_handler(httpd_req_t *req);
httpd_handle_t start_webserver(void);
void wifi_init_softap(void);
void init_networking(void);

#endif