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
#include "profile.h"
#include "timer.h"
#include "scheduler.h"

void app_main(void)
{

    init_monitoring_uart();
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
    delay_ms(2000);
    ESP_LOGI(GENERAL_TAG, "Initializing Devices");
    init_led();
    lcd_init();
    init_networking();
    init_adc1_shared();
    init_ldr();
    init_moisture();
    init_pumps();
    init_flow_sensors();
    init_tasks();
    blink_5();
    ESP_LOGI(GENERAL_TAG, "BOOTED");
    lcd_write_string("BOOTED!");
    delay_ms(2000);
    lcd_clear();
    delay_ms(10);
    lcd_set_cursor(0, 0);
    lcd_write_string("READING STATES...");

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
        ESP_LOGI(GENERAL_TAG, "++++++++++++++");
        // delay_ms(3000);

        pump_on(1);
        pump_on(2);
        delay_ms(1500);
        pump_off(1);
        pump_off(2);
        delay_ms(1500);
    }
}