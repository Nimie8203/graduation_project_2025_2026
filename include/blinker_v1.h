#ifndef BLINKER_V1_H
#define BLINKER_V1_H

#define LED_PIN 2

void led_init(void);
void led_on(void);
void led_off(void);
bool led_get_state(void);
void led_print_state(void);

void blink_5_task(void);
void blink_n_task(int8_t count);
void blink_nima(void);
void blink_aseel(void);
// HUDA SHALL ADD HER BLINK MODE


#endif