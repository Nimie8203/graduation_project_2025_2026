#include "tasks.h"
static void lcd_task(void *arg)
{
    delay_ms(4000);

    char line1[17];
    char line2[17];

    const char dot_chars[7][7] = {
        "      ",
        ".     ",
        "..    ",
        "...   ",
        "....  ",
        "..... ",
        "......",
    };

    uint8_t dot_count = 0;
    uint32_t flow_average = 0;
    uint32_t moist_average = 0;
    while (1)
    {
        flow_average = (uint32_t)(g_state.flow_sens_1 + g_state.flow_sens_2) / 2;
        moist_average = (uint32_t)(g_state.moisture_1 + g_state.moisture_2 + g_state.moisture_3 + g_state.moisture_4) / 4;

        // Clamp to 2-digit display range
        uint32_t f = flow_average > 99 ? 99 : flow_average;
        uint32_t m = moist_average > 99 ? 99 : moist_average;
        unsigned int l = (unsigned int)g_state.light_intensity > 99 ? 99 : (unsigned int)g_state.light_intensity;

        int t = (int)(g_state.temperature / 10);
        int h = (int)(g_state.humidity / 10);
        if (t < -9)
            t = -9;
        if (t > 99)
            t = 99;
        if (h < -9)
            h = -9;
        if (h > 99)
            h = 99;

        // Line 1
        snprintf(line1, sizeof(line1), "F:%-2" PRIu32 " M:%-2" PRIu32 " L:%-2u ",
                 f, m, l);

        // Line 2
        snprintf(line2, sizeof(line2), "T:%-2d H:%-2d %s",
                 t, h, dot_chars[dot_count]);

        lcd_set_cursor(0, 0);
        lcd_write_string(line1);

        lcd_set_cursor(1, 0);
        lcd_write_string(line2);

        dot_count = (dot_count + 1) % 7;
        delay_ms(300);
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

        delay_ms(10000);
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