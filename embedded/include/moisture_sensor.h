#ifndef MOISTURE_SENSOR_H
#define MOISTURE_SENSOR_H

#include "esp_idf_common.h"

#define MOIST_POWER_PIN 19 // PROBLEM

#define MOIST_SENS_1_CHANNEL ADC_CHANNEL_5
#define MOIST_SENS_2_CHANNEL ADC_CHANNEL_4
#define MOIST_SENS_3_CHANNEL ADC_CHANNEL_7
#define MOIST_SENS_4_CHANNEL ADC_CHANNEL_6

void moisture_init(void);
void moisture_read_all(void);

#endif