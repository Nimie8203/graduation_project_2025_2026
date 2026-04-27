#include "pump.h"

void init_pumps()
{
    gpio_reset_pin(PUMP_1_PIN);
    gpio_reset_pin(PUMP_2_PIN);

    gpio_config_t pump_conf = {
        .pin_bit_mask = (1ULL << PUMP_1_PIN) | (1ULL << PUMP_2_PIN),
        .mode         = GPIO_MODE_OUTPUT,
        .pull_up_en   = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type    = GPIO_INTR_DISABLE,
    };
    gpio_config(&pump_conf);

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