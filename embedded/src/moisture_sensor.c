#include "moisture_sensor.h"
#include "esp_idf_common.h"

#define SAMPLES 5

static int compare(const void *a, const void *b)
{
    return (*(int*)a - *(int*)b);
}

static int read_median(adc1_channel_t channel)
{
    int buffer[SAMPLES];

    for (int i = 0; i < SAMPLES; i++)
    {
        buffer[i] = adc1_get_raw(channel);
    }

    qsort(buffer, SAMPLES, sizeof(int), compare);

    return buffer[SAMPLES / 2];
}

static int compare(const void *a, const void *b)
{
    return (*(int*)a - *(int*)b);
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

void moisture_init(void)
{
    adc1_config_width(ADC_WIDTH_BIT_12);

    adc1_config_channel_atten(MOIST_SENS_1_CHANNEL, ADC_ATTEN_DB_11);
    adc1_config_channel_atten(MOIST_SENS_2_CHANNEL, ADC_ATTEN_DB_11);
    adc1_config_channel_atten(MOIST_SENS_3_CHANNEL, ADC_ATTEN_DB_11);
    adc1_config_channel_atten(MOIST_SENS_4_CHANNEL, ADC_ATTEN_DB_11);

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
