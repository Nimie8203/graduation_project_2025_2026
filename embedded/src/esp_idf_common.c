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

adc_oneshot_unit_handle_t g_adc1_handle = NULL;

void init_adc1_shared(void)
{
    if (g_adc1_handle != NULL) return;

    adc_oneshot_unit_init_cfg_t unit_cfg = {
        .unit_id = ADC_UNIT_1,
    };
    ESP_ERROR_CHECK(adc_oneshot_new_unit(&unit_cfg, &g_adc1_handle));
}

void delay_ms(uint32_t time)
{
    vTaskDelay(time / portTICK_PERIOD_MS);
}