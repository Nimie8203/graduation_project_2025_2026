#include "esp_idf_common.h"
#include "led.h"
#include "networking.h"
#include "lcd.h"
#include "t_h_sensor.h"
#include "pump.h"
#include "flow_sensor.h"
#include "moisture_sensor.h"
#include "ldr_sensor.h"
#include "tasks.h"

void app_main(void)
{
    delay_ms(1000);
    
    ESP_LOGI(GENERAL_TAG, "Initializing Devices");
    init_networking();
    init_adc1_shared();
    init_pumps();
    init_flow_sensors();
    init_led();
    init_tasks();
    blink_5();
    ESP_LOGI(GENERAL_TAG, "BOOTED");
    while (1)
    {
        ESP_LOGI(TEMP_TAG, "%d", g_state.temperature);
        ESP_LOGI(HUM_TAG, "%d", g_state.humidity);
        ESP_LOGI(FLOW_TAG, "%d", g_state.flow_sens_1);
        ESP_LOGI(FLOW_TAG, "%d", g_state.flow_sens_2);
        ESP_LOGI(LDR_TAG, "%d", g_state.light_intensity);
        ESP_LOGI(MOIST_TAG, "%d", g_state.moisture_1);
        ESP_LOGI(MOIST_TAG, "%d", g_state.moisture_2);
        ESP_LOGI(MOIST_TAG, "%d", g_state.moisture_3);
        ESP_LOGI(MOIST_TAG, "%d", g_state.moisture_4);
        ESP_LOGI(GENERAL_TAG, "++++++++++++++");
        delay_ms(3000);
    }
}