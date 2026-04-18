#include "esp_idf_common.h"
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
#include "tasks.h"

void app_main(void)
{
    ESP_LOGI(GENERAL_TAG, "Initializing Devices");
    init_networking();
    lcd_init();
    init_pumps();
    init_led();
    init_tasks();
    blink_5();
    ESP_LOGI(GENERAL_TAG, "BOOTED");
    lcd_write_string("BOOT COMPLETE!");

    while (1)
    {
        read_th(&g_state.humidity, &g_state.temperature);

        delay_ms(2000);
    }
}