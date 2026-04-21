#include "pump.h"

void init_pumps()
{
    gpio_set_direction(PUMP_1_PIN, GPIO_MODE_OUTPUT);
    gpio_set_direction(PUMP_2_PIN, GPIO_MODE_OUTPUT);
    gpio_set_level(PUMP_1_PIN, 0);
    gpio_set_level(PUMP_2_PIN, 0);
    g_state.pump_1_state = false;
    g_state.pump_2_state = false;

    ESP_LOGI(PUMP_TAG, "PUMPS Initiated");
}

void pump_on(PUMP pump_num)
{

    switch (pump_num)
    {
    case PUMP_1:
        gpio_set_level(PUMP_1_PIN, 1);
        g_state.pump_1_state = true;
        ESP_LOGI(PUMP_TAG, "PUMP 1 ON");
        break;
    case PUMP_2:
        gpio_set_level(PUMP_2_PIN, 1);
        g_state.pump_2_state = true;
        ESP_LOGI(PUMP_TAG, "PUMP 2 ON");
        break;
    default:
        ESP_LOGW(PUMP_TAG, "Unknown pump: %d", pump_num);
        break;
    }
}

void pump_off(PUMP pump_num)
{
    switch (pump_num)
    {
    case PUMP_1:
        gpio_set_level(PUMP_1_PIN, 0);
        g_state.pump_1_state = false;
        ESP_LOGI(PUMP_TAG, "PUMP 1 OFF");
        break;
    case PUMP_2:
        gpio_set_level(PUMP_2_PIN, 0);
        g_state.pump_2_state = false;
        ESP_LOGI(PUMP_TAG, "PUMP 2 OFF");
        break;
    default:
        ESP_LOGW(PUMP_TAG, "Unknown pump: %d", pump_num);
        break;
    }
}