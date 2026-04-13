#ifndef BLINKER_V1_H
#define BLINKER_V1_H

#include "esp_idf_common.h"
#include <stdbool.h>


void led_init(void);
void led_on(void);
void led_off(void);
bool led_get_state(void);
void led_print_state(void);
void led_command_indicate(void);

void blink_5_task(void);
void blink_n_task(int8_t count);
void blink_nima(void);
void blink_aseel(void);
void huda_blink(void);

void blink_wifi(bool is_wifi_on);

#endif