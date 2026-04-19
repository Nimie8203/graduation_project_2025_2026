#include "tasks.h"
#include "t_h_sensor.h"
#include "flow_sensor.h"
#include "led.h"
#include "lcd.h"

static void lcd_task(void *arg)
{
    lcd_init(); 
    lcd_set_cursor(0, 0);          
    lcd_write_string("BOOTED!");
    vTaskDelete(NULL);
}

static void sensor_task(void *arg)
{
    while (1)
    {
        read_th(&g_state.humidity, &g_state.temperature);
        read_flow_sens();
        delay_ms(2000);
    }
}

static void wifi_led_indicator(void *arg)
{
    while (1)
    {
        if(g_state.wifi_state) blink_wifi();
        else
        {
            led_off();
            delay_ms(500);
        }
    }
}

static void irrigation_task(void *arg)
{
    while (1)
    {
        delay_ms(30000);
    }
}
void init_tasks(void)
{
    xTaskCreate(lcd_task, "lcd_task", 2048, NULL, 6, NULL);
    xTaskCreate(sensor_task, "sensor_task", 3072, NULL, 5, NULL);
    xTaskCreate(irrigation_task, "irrigation_task", 3072, NULL, 5, NULL);
    xTaskCreate(wifi_led_indicator, "wifi_led_indicator_task", 3072, NULL, 5, NULL);
}