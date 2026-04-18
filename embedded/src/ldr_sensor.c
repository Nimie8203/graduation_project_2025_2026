#include "ldr_sensor.h"
#include <stdlib.h>

#define SAMPLES 5

static int compare(const void *a, const void *b)
{
    return (*(uint16_t*)a - *(uint16_t*)b);
}

static uint16_t read_median(adc1_channel_t channel)
{
    uint16_t buffer[SAMPLES];

    for (int i = 0; i < SAMPLES; i++)
    {
        buffer[i] = (uint16_t)adc1_get_raw(channel);
    }

    qsort(buffer, SAMPLES, sizeof(uint16_t), compare);

    return buffer[SAMPLES / 2];
}

void ldr_init(void)
{
    adc1_config_width(ADC_WIDTH_BIT_12);
    adc1_config_channel_atten(LDR_CHANNEL, ADC_ATTEN_DB_11);
}

void ldr_read(void)
{
    uint16_t filtered_value = read_median(LDR_CHANNEL);

    g_state.light_intensity = 100 - ((filtered_value * 100) / 4096);
}