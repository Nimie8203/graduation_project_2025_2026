#include "esp_idf_common.h"

status_t g_state = {0};

const char *LED_TAG = "LED";
const char *PUMP_TAG = "PUMP";
const char *WIFI_TAG = "WIFI";
const char *TEMP_TAG = "DHT_TEMP";
const char *HUM_TAG = "DHT_HUM";
const char *LDR_TAG = "LDR_SENS";
const char *FLOW_TAG = "FLOW_SENS";
const char *MOIST_TAG = "MOIST_SENS";
const char *TANK_TAG = "TANK";
const char *PIPE_TAG = "PIPE";
const char *PROFILE_TAG = "PROFILE";
const char *IRRIG_TAG = "IRRIGATOR";
const char *GENERAL_TAG = "GENERAL";

adc_oneshot_unit_handle_t g_adc1_handle = NULL;

void init_states(void)
{
    g_state.flow_sens_1 = 0;
    g_state.flow_sens_2 = 0;
    g_state.pump_1_state = 0;
    g_state.pump_2_state = 0;
    g_state.temperature = 0;
    g_state.humidity = 0;
    g_state.moisture_1 = 0;
    g_state.moisture_2 = 0;
    g_state.moisture_3 = 0;
    g_state.moisture_4 = 0;
    g_state.light_intensity = 0;
    g_state.profile = (profile_t){
        .profile_name = "default",
        .plant_name = "general",
        .moisture_threshold = 20
    };
}

void init_adc1_shared(void)
{
    if (g_adc1_handle != NULL)
        return;

    adc_oneshot_unit_init_cfg_t unit_cfg = {
        .unit_id = ADC_UNIT_1,
    };
    ESP_ERROR_CHECK(adc_oneshot_new_unit(&unit_cfg, &g_adc1_handle));
}

void delay_ms(uint32_t time)
{
    vTaskDelay(time / portTICK_PERIOD_MS);
}