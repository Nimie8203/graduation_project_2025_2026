#ifndef PUMP_H
#define PUMP_H

#include "esp_idf_common.h"

typedef enum {
    PUMP_1 = 1,
    PUMP_2
} PUMP;

#define PUMP_1_PIN 1
#define PUMP_2_PIN 3

void init_pumps(uint8_t pump_1_pin, uint8_t pump_2_pin);
void pump_on(PUMP pump_num);
void pump_off(PUMP pump_num);

#endif