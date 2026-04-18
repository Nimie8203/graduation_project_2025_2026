#ifndef LED_H
#define LED_H

#include "esp_idf_common.h"

#define LED_PIN 2

void init_led(void);
void led_on(void);
void led_off(void);
void led_command_indicate(void);

void blink_5(void);
void blink_n(int8_t count);
void blink_nima(void);
void blink_aseel(void);
void blink_huda(void);

void blink_wifi(bool is_wifi_on);

#endif