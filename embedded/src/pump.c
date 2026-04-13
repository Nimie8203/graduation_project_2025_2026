#include "pump.h"

void init_pumps()
{
    gpio_set_direction(PUMP_1_PIN, GPIO_MODE_OUTPUT);
    gpio_set_direction(PUMP_2_PIN, GPIO_MODE_OUTPUT);
    ESP_LOGI(PUMP_TAG, "PUMPS Initiated");
}

void pump_on(PUMP pump_num)
{

    if (pump_num == 1)
    {
        gpio_set_level(PUMP_1_PIN, 1);
        g_status.pump_1_state = true;
        ESP_LOGI(PUMP_TAG, "PUMP 1 ON");
    }
    if (pump_num == 2)
    {
        gpio_set_level(PUMP_2_PIN, 1);
        g_status.pump_2_state = true;
        ESP_LOGI(PUMP_TAG, "PUMP 2 ON");
    }
}

void pump_off(PUMP pump_num)
{
    if (pump_num == 1)
    {
        gpio_set_level(PUMP_1_PIN, 0);
        g_status.pump_1_state = false;
        ESP_LOGI(PUMP_TAG, "PUMP 1 OFF");
    }
    if (pump_num == 2)
    {
        gpio_set_level(PUMP_2_PIN, 0);
        g_status.pump_2_state = false;
        ESP_LOGI(PUMP_TAG, "PUMP 2 OFF");
    }
}