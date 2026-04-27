#include "led.h"
#include "serial_input.h"

void init_led(void)
{
    gpio_reset_pin(LED_PIN);
    gpio_config_t led_config = {
        .pin_bit_mask = (1ULL << LED_PIN),
        .mode         = GPIO_MODE_OUTPUT,
        .pull_up_en   = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type    = GPIO_INTR_DISABLE,
    };
    gpio_config(&led_config);

    led_off();
    ESP_LOGI(LED_TAG, "LED Initiated");
}

void led_on(void)
{
    gpio_set_level(LED_PIN, 1);
    g_state.led_state = true;
    //ESP_LOGI(LED_TAG, "LED On");
}

void led_off(void)
{
    gpio_set_level(LED_PIN, 0);
    g_state.led_state = false;
    //ESP_LOGI(LED_TAG, "LED Off");
}

void led_command_indicate(void)
{
    led_off();
    delay_ms(500);
    for (int8_t i = 1; i <= 5; i++)
    {
        led_on();
        delay_ms(100);
        led_off();
    }
}

void blink_5(void)
{
    uint8_t count = 1;
    while (count <= 5)
    {
        led_on();
        delay_ms(50);
        led_off();
        delay_ms(100);
        count++;
    }
}

void blink_n(int8_t count)
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
            delay_ms(125);
            led_off();
            delay_ms(250);
            iteration++;
        }
    }
    else
    {
        while (iteration <= count)
        {
            printf("\nDEBUG = %d\n", iteration);
            led_on();
            delay_ms(125);
            led_off();
            delay_ms(250);
            iteration++;
        }
    }
}

void blink_nima(void)
{
    printf("\nBlinking Nima...\n");
    delay_ms(1000);

    led_on();
    delay_ms(600);
    led_off();
    delay_ms(200);
    led_on();
    delay_ms(200);
    led_off();
}

void blink_aseel()
{
    printf("\nBlinking Aseel...\n");
    delay_ms(1000);

    led_on();
    delay_ms(200);
    led_off();
    delay_ms(200);
    led_on();
    delay_ms(600);
    led_off();
}

void blink_huda()
{
    printf("\nBlinking Huda...\n");
    delay_ms(1000);

    led_on();
    delay_ms(200);
    led_off();
    delay_ms(200);
    led_on();
    delay_ms(200);
    led_off();
    delay_ms(200);
    led_on();
    delay_ms(200);
    led_off();
    delay_ms(200);
    led_on();
    delay_ms(200);
    led_off();
}

void blink_wifi()
{
    led_on();
    delay_ms(700);
    led_off();
    delay_ms(100);
    led_on();
    delay_ms(100);
    led_off();
    delay_ms(1000);
}