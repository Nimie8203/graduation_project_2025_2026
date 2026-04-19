#ifndef PUMP_H
#define PUMP_H

#include "esp_idf_common.h"

typedef enum {
    PUMP_1 = 1,
    PUMP_2
} PUMP;

#define PUMP_1_PIN 17
#define PUMP_2_PIN 16

void init_pumps();
void pump_on(PUMP pump_num);
void pump_off(PUMP pump_num);

#endif