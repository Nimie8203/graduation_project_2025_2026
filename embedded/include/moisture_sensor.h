#ifndef MOISTURE_SENSOR_H
#define MOISTURE_SENSOR_H

#include "driver/adc.h"
#include "esp_idf_common.h"

#define MOIST_POWER_PIN 4

<<<<<<< HEAD
#define MOIST_SENS_1_CHANNEL ADC1_CHANNEL_5  
#define MOIST_SENS_2_CHANNEL ADC1_CHANNEL_4  
#define MOIST_SENS_3_CHANNEL ADC1_CHANNEL_7  
#define MOIST_SENS_4_CHANNEL ADC1_CHANNEL_6  

void moisture_init(void);
void moisture_read_all(void);
=======
#define MOIST_SENS_1_PIN 33
#define MOIST_SENS_2_PIN 32
#define MOIST_SENS_3_PIN 35
#define MOIST_SENS_4_PIN 34
>>>>>>> origin/main

#endif