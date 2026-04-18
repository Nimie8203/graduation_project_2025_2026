#include "moisture_sensor.h"

#define SAMPLES 5

static int compare(const void *a, const void *b)
{
    return (*(int*)a - *(int*)b);
}

static adc_channel_t moisture_channels[] = {
    MOIST_SENS_1_CHANNEL,
    MOIST_SENS_2_CHANNEL,
    MOIST_SENS_3_CHANNEL,
    MOIST_SENS_4_CHANNEL,
};

static uint16_t read_median(adc_channel_t channel)
{
    int buffer[SAMPLES];

    for (int i = 0; i < SAMPLES; i++)
    {
        ESP_ERROR_CHECK(adc_oneshot_read(g_adc1_handle, channel, &buffer[i]));
    }

    qsort(buffer, SAMPLES, sizeof(int), compare);

    return (uint16_t)buffer[SAMPLES / 2];
}

void moisture_init(void)
{
    adc_oneshot_chan_cfg_t chan_cfg = {
        .atten    = ADC_ATTEN_DB_12,
        .bitwidth = ADC_BITWIDTH_12,
    };

    for (int i = 0; i < 4; i++)
    {
        ESP_ERROR_CHECK(adc_oneshot_config_channel(g_adc1_handle,
                                                   moisture_channels[i],
                                                   &chan_cfg));
    }

    gpio_set_direction(MOIST_POWER_PIN, GPIO_MODE_OUTPUT);
    gpio_set_level(MOIST_POWER_PIN, 0);
}

void moisture_read_all(void)
{
    gpio_set_level(MOIST_POWER_PIN, 1);

    uint16_t m1 = read_median(MOIST_SENS_1_CHANNEL);
    uint16_t m2 = read_median(MOIST_SENS_2_CHANNEL);
    uint16_t m3 = read_median(MOIST_SENS_3_CHANNEL);
    uint16_t m4 = read_median(MOIST_SENS_4_CHANNEL);

    g_state.moisture_1 = (m1 * 100) / 4096;
    g_state.moisture_2 = (m2 * 100) / 4096;
    g_state.moisture_3 = (m3 * 100) / 4096;
    g_state.moisture_4 = (m4 * 100) / 4096;

    gpio_set_level(MOIST_POWER_PIN, 0);
}