#include "blinker_v1.h"

void init_led(void)
{
    gpio_set_direction(LED_PIN, GPIO_MODE_OUTPUT);
    led_off();
    ESP_LOGI(LED_TAG, "LED Initated");
}

void led_on(void)
{
    gpio_set_level(LED_PIN, 1);
    g_state.led_state = true;
    ESP_LOGI(LED_TAG, "LED On");
}

void led_off(void)
{
    gpio_set_level(LED_PIN, 0);
    g_state.led_state = false;
    ESP_LOGI(LED_TAG, "LED Off");
}

void led_command_indicate(void)
{
    led_off();
    delay(500);
    for (int8_t i = 1; i <= 5; i++)
    {
        led_on();
        delay(100);
        led_off();
    }
}

void blink_5_task(void)
{
    uint8_t count = 1;
    while (count <= 5)
    {
        led_on();
        delay(50);
        led_off();
        delay(100);
        count++;
    }
}

void blink_n_task(int8_t count)
{
    uint8_t iteration = 1;
    if (count == -1)
    {
        uint8_t usr_count;
        char input[32];
        get_serial_input(input, sizeof(input));
        printf("\nYour input was %s\n", input);

        usr_count = atoi(input);

        while (iteration <= usr_count)
        {
            printf("\n%d\n", iteration);
            led_on();
            delay(125);
            led_off();
            delay(250);
            iteration++;
        }
    }
    else
    {
        while (iteration <= count)
        {
            printf("\nDEBUG = %d\n", iteration);
            led_on();
            delay(125);
            led_off();
            delay(250);
            iteration++;
        }
    }
}

void blink_nima(void)
{
    printf("\nBlinking Nima...\n");
    delay(1000);

    led_on();
    delay(600);
    led_off();
    delay(200);
    led_on();
    delay(200);
    led_off();
}

void blink_aseel()
{
    printf("\nBlinking Aseel...\n");
    delay(1000);

    led_on();
    delay(200);
    led_off();
    delay(200);
    led_on();
    delay(600);
    led_off();
}

void blink_huda()
{
    printf("\nBlinking Huda...\n");
    delay(1000);

    led_on();
    delay(200);
    led_off();
    delay(200);
    led_on();
    delay(200);
    led_off();
    delay(200);
    led_on();
    delay(200);
    led_off();
    delay(200);
    led_on();
    delay(200);
    led_off();
}

void blink_wifi(bool is_wifi_on)
{
    while (is_wifi_on)
    {
        led_on();
        delay(700);
        led_off();
        delay(100);
        led_on();
        delay(100);
        led_off();
        delay(1000);
    }
}