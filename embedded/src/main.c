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
#include "uart.h"

uint8_t line_1_avg = 0;
uint8_t line_2_avg = 0;

void app_main(void)
{
    delay_ms(5000);
    init_monitoring_uart();
    delay_ms(1000);
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
    ESP_LOGI(GENERAL_TAG, "Initializing Devices");
    init_led();
    init_lcd();
    init_networking();
    init_adc1_shared();
    init_ldr();
    init_moisture();
    init_pumps();
    init_flow_sensors();
    init_states();
    init_tasks();
    blink_5();
    ESP_LOGI(GENERAL_TAG, "BOOTED");
    lcd_write_string("BOOTED!");
    delay_ms(2000);
    lcd_clear();
    delay_ms(10);
    lcd_set_cursor(0, 0);
    lcd_write_string("READING STATES...");
    delay_ms(2000);
    lcd_clear();
    delay_ms(10);

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
        ESP_LOGI(PUMP_TAG, "%d", g_state.pump_1_state);
        ESP_LOGI(PUMP_TAG, "%d", g_state.pump_2_state);
        ESP_LOGI(TANK_TAG, "%d", g_state.tank_state);
        ESP_LOGI(PIPE_TAG, "%d", g_state.pipe_state);
        ESP_LOGI(PROFILE_TAG, "%s", g_state.profile.profile_name);
        ESP_LOGI(PROFILE_TAG, "%s", g_state.profile.plant_name);
        ESP_LOGI(GENERAL_TAG, "++++++++++++++");

        line_1_avg = (uint8_t)(g_state.moisture_1 + g_state.moisture_2) / 2;
        line_2_avg = (uint8_t)(g_state.moisture_3 + g_state.moisture_4) / 2;

        if (line_1_avg < g_state.profile.moisture_threshold && line_2_avg < g_state.profile.moisture_threshold)
        {
            pump_on(1);
            pump_on(2);
            ESP_LOGI(IRRIG_TAG, "Irrigating both lines");
        }
        else if (line_1_avg < g_state.profile.moisture_threshold && line_2_avg > g_state.profile.moisture_threshold)
        {
            pump_on(1);
            ESP_LOGI(IRRIG_TAG, "Irrigating line 1");
        }
        else if (line_2_avg < g_state.profile.moisture_threshold && line_1_avg > g_state.profile.moisture_threshold)
        {
            pump_on(2);
            ESP_LOGI(IRRIG_TAG, "Irrigating line 2");
        }
        else if (line_1_avg > g_state.profile.moisture_threshold && line_2_avg > g_state.profile.moisture_threshold)
        {
            pump_off(1);
            pump_off(2);
            ESP_LOGI(IRRIG_TAG, "No irrigating");
        }
        delay_ms(2000);
    }
}