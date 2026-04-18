#include "esp_idf_common.h"

void app_main(void)
{
    ESP_LOGI(GENERAL_TAG, "Initializing Devices");
    init_networking();
    init_pumps();
    init_led();
    blink_5_task();
    ESP_LOGI(GENERAL_TAG, "BOOTED");

    while (1)
    {
        read_th(&g_state.humidity, &g_state.temperature);

        delay_ms(2000);
    }
}