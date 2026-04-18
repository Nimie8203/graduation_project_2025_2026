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
#include <stdbool.h>

// CUSTOM HEADERS
#include "led.h"
#include "serial_input.h"
#include "networking.h"
#include "button.h"
#include "lcd.h"
#include "t_h_sensor.h"
#include "pump.h"
#include "flow_sensor.h"
#include "moisture_sensor.h"
#include "ldr_sensor.h"

// EXTERNAL LIBRARYS AND DRIVERS
#include "dht.h"

// STATUS
typedef struct
{
    int16_t temperature;
    int16_t humidity;
    uint16_t light_intensity;
    uint32_t flow_sens_1;
    uint32_t flow_sens_2;
    int16_t moisture_1;
    int16_t moisture_2;
    int16_t moisture_3;
    int16_t moisture_4;
    bool led_state;
    bool pump_1_state;
    bool pump_2_state;
    bool wifi_state;

} device_status_t;

extern device_status_t g_state;

// PROFILE

#define MAX_NAME_LENGTH 32
#define MAX_IRRIG_TIME_PER_DAY 24

typedef struct
{
    char profile_name[MAX_NAME_LENGTH];
    char plant_name[MAX_NAME_LENGTH];
    uint8_t irrig_times_per_day;
    uint16_t times_of_day[MAX_IRRIG_TIME_PER_DAY]; // UNIT IS MINUTES FROM MIDNIGHT
    uint16_t water_amount_per_irrig;
    uint8_t moisture_threshold;
    // COULD ADD MORE CONDITIONAL STUFF LIKE ONLY WATER WHEN TEMP IS ABOVE 0 OR SOMETHING LIKE THAT
} profile_t;

// TAGS FOR DEBUG

extern const char *LED_TAG;
extern const char *PUMP_TAG;
extern const char *WIFI_TAG;
extern const char *TEMP_TAG;
extern const char *HUM_TAG;
extern const char *LDR_TAG;
extern const char *FLOW_TAG;
extern const char *MOIST_TAG;
extern const char *GENERAL_TAG;

// SHORTCUT FUNCTIONS
void delay_ms(uint32_t time);

#endif