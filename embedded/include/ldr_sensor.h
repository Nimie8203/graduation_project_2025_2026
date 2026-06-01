#ifndef LDR_SENSOR_H
#define LDR_SENSOR_H

#include "esp_idf_common.h"

#define LDR_PIN 39
#define LDR_CHANNEL ADC_CHANNEL_3

void init_ldr(void);
void read_ldr(void);

#endif