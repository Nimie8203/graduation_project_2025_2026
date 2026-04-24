#ifndef FLOW_SENSOR_H
#define FLOW_SENSOR_H

#include "esp_idf_common.h"

#define FLOW_SENS_1_PIN 22
#define FLOW_SENS_2_PIN 23

#define PULSES_PER_LITER 50
#define SAMPLE_PERIOD 1000  // UNIT MILLISECONDS

typedef enum {
    FS_1,
    FS_2
} FLOW_SENS_NUM;

void init_flow_sensors(void);
void read_flow_sens(void);

#endif