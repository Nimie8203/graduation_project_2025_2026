#ifndef T_H_SENSOR_H 
#define T_H_SENSOR_H

#include "esp_idf_common.h"
#include "dht.h"

#define DHT_PIN 19
#define DHT_TYPE 0

void read_th(int16_t *hum  , int16_t *temp);



#endif
