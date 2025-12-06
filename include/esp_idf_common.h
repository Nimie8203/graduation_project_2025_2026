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

// C HEADERS
#include <stdio.h>
#include <string.h>
#include <ctype.h>

// CUSTOM HEADERS
#include "blinker_v1.h"
#include "serial_input.h"
#include "networking.h"

// SHORTCUT FUNCTIONS
void delay(uint16_t time);

#endif