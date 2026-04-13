#include "pump.h"

void init_pumps(uint8_t pump_1_pin, uint8_t pump_2_pin)
{
    gpio_set_direction(pump_1_pin, GPIO_MODE_OUTPUT);
    gpio_set_direction(pump_2_pin, GPIO_MODE_OUTPUT);
}

void pump_on(PUMP pump_num)
{

    if (pump_num == 1)
    {
        gpio_set_level(PUMP_1_PIN, 1);
    }
    if (pump_num == 2)
    {
        gpio_set_level(PUMP_2_PIN, 1);
    }
}

void pump_off(PUMP pump_num)
{
    if (pump_num == 1)
    {
        gpio_set_level(PUMP_1_PIN, 0);
    }
    if (pump_num == 2)
    {
        gpio_set_level(PUMP_2_PIN, 0);
    }
}