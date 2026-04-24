#ifndef NETWORKING_H
#define NETWORKING_H

#include "esp_idf_common.h"
#include "cJSON.h"
#include "pump.h"
#include "led.h"

#define ESP32_SSID "ESP32_AP"
#define ESP32_PASS "82138213"

void init_networking(void);

#endif