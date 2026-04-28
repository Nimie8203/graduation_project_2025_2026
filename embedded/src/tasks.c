#include "tasks.h"

static void lcd_task(void *arg)
{
    while (1)
    {
        lcd_set_cursor(0, 0);
        // display g_state values here, e.g.:
        // char buf[16];
        // snprintf(buf, sizeof(buf), "T:%d H:%d", g_state.temperature, g_state.humidity);
        // lcd_write_string(buf);

        delay_ms(1000);
    }
}
static void state_task(void *arg)
{
    while (1)
    {
        read_th(&g_state.humidity, &g_state.temperature);
        read_flow_sens();
        read_tank();
        read_pipe();
        read_ldr();
        read_moisture();
        
        delay_ms(3000);
    }
}

static void wifi_led_indicator(void *arg)
{
    while (1)
    {
        if (g_state.wifi_state)
            blink_wifi();
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
    xTaskCreate(lcd_task, "lcd_task", 4096, NULL, 6, NULL);
    xTaskCreate(state_task, "state_task", 3072, NULL, 5, NULL);
    xTaskCreate(irrigation_task, "irrigation_task", 3072, NULL, 5, NULL);
    xTaskCreate(wifi_led_indicator, "wifi_led_indicator_task", 3072, NULL, 5, NULL);
}