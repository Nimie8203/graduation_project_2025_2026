#ifndef ESP_IDF_COMMON_H
#define ESP_IDF_COMMON_H

// ESP HEADERS
#include "driver/gpio.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_wifi.h"
#include "esp_log.h"
#include "esp_event.h"
#include "nvs_flash.h"
#include "esp_netif.h"
#include "esp_http_server.h"

#define LED_PIN 2

// C HEADERS
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>

// CUSTOM HEADERS
#include "blinker_v1.h"
#include "serial_input.h"
#include "networking.h"
#include "button.h"
#include "lcd.h"
#include "t_h_sensor.h"
#include "pump.h"

// EXTERNAL LIBRARYS AND DRIVERS
#include "dht.h"

// STATUS
typedef struct
{
    int16_t temperature;
    int16_t humidity;
    int16_t light_intensity;
    bool led_state;
    bool pump_1_state;
    bool pump_2_state;
    bool wifi_state;
    
} device_status_t;

device_status_t g_state = {0};

// TAGS FOR DEBUG

const char *LED_TAG = "LED";
const char *PUMP_TAG = "PUMP";
const char *WIFI_TAG = "WIFI";
const char *TEMP_TAG = "DHT_TEMP";
const char *HUM_TAG = "DHT_HUM";
const char *LDR_TAG = "LDR_SENS";
const char *FLOW_TAG = "FLOW_SENS";
const char *MOIST_TAG = "MOIST_SENS";
const char *GENERAL_TAG = "GENERAL";

// SHORTCUT FUNCTIONS
void delay(uint16_t time);

#endif