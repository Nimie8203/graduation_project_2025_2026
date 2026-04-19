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
    //init_adc1_shared();
    lcd_init();
    lcd_clear();//*
    lcd_set_cursor(0,0); //*
    lcd_write_string("BOOT COMPLETE!"); //*
    init_pumps();
    init_flow_sensors();
    init_led();
    init_tasks();
    blink_5();
    ESP_LOGI(GENERAL_TAG, "BOOTED");
    while (1)
    {
        pump_on(PUMP_2); 

        delay_ms(2000);
        pump_off(PUMP_2);
        delay_ms(2000);
    }
}