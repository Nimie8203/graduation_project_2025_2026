#ifndef LDR_SENSOR_H
#define LDR_SENSOR_H

#include "esp_idf_common.h"

#define LDR_PIN     36
#define LDR_CHANNEL ADC_CHANNEL_0

void ldr_init(void);
void ldr_read(void);

#endif