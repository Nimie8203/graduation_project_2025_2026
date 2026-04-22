#include "ldr_sensor.h"

#define SAMPLES 5

static int compare(const void *a, const void *b)
{
    return (*(int*)a - *(int*)b);  // fix: was casting to uint16_t*, causing signed compare bug
}

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

void init_ldr(void)
{
    adc_oneshot_chan_cfg_t chan_cfg = {
        .atten    = ADC_ATTEN_DB_12,
        .bitwidth = ADC_BITWIDTH_12,
    };
    ESP_ERROR_CHECK(adc_oneshot_config_channel(g_adc1_handle, LDR_CHANNEL, &chan_cfg));
}

void read_ldr(void)
{
    uint16_t filtered_value = read_median(LDR_CHANNEL);
    g_state.light_intensity = (filtered_value * 100) / 4096;
    //g_state.light_intensity = filtered_value;
    
}