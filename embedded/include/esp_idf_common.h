#ifndef ESP_IDF_COMMON_H
#define ESP_IDF_COMMON_H

// ESP HEADERS
#include "driver/gpio.h"
#include "driver/uart.h"
#include "esp_adc/adc_oneshot.h"
#include "esp_adc/adc_cali.h"
#include "esp_adc/adc_cali_scheme.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_wifi.h"
#include "esp_log.h"
#include "esp_event.h"
#include "nvs_flash.h"
#include "nvs.h"
#include "esp_netif.h"
#include "esp_http_server.h"
#include "rom/ets_sys.h"
#include "cJSON.h"


// C HEADERS
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>
#include <sys/time.h>
#include <time.h>
#include <string.h>
#include <stdio.h>

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
    bool tank_state;
    bool pipe_state;

} device_status_t;

extern device_status_t g_state;
extern adc_oneshot_unit_handle_t g_adc1_handle;

// TAGS FOR DEBUG
extern const char *LED_TAG;
extern const char *PUMP_TAG;
extern const char *WIFI_TAG;
extern const char *TEMP_TAG;
extern const char *HUM_TAG;
extern const char *LDR_TAG;
extern const char *FLOW_TAG;
extern const char *MOIST_TAG;
extern const char *TANK_TAG;
extern const char *PIPE_TAG;
extern const char *GENERAL_TAG;
extern const char *TIMER_TAG;


// SHORTCUT FUNCTIONS
void delay_ms(uint32_t time);
void init_adc1_shared(void);


#endif