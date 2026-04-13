#include "t_h_sensor.h"

/*
read sensor
*/

void read_th(int16_t *hum  , int16_t *temp) {
    dht_read_data(DHT_TYPE, DHT_PIN, hum, temp);
    led_command_indicate();
}

