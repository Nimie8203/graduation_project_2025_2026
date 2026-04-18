#include "esp_idf_common.h"

device_status_t g_state = {0};

const char *LED_TAG = "LED";
const char *PUMP_TAG = "PUMP";
const char *WIFI_TAG = "WIFI";
const char *TEMP_TAG = "DHT_TEMP";
const char *HUM_TAG = "DHT_HUM";
const char *LDR_TAG = "LDR_SENS";
const char *FLOW_TAG = "FLOW_SENS";
const char *MOIST_TAG = "MOIST_SENS";
const char *GENERAL_TAG = "GENERAL";

void delay_ms(uint32_t time)
{
    vTaskDelay(time / portTICK_PERIOD_MS);
}