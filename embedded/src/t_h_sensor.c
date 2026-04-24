#include "t_h_sensor.h"

void read_th(int16_t *hum, int16_t *temp)
{
    dht_read_data(DHT_TYPE, DHT_PIN, hum, temp);
}