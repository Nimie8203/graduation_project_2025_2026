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
        read_th(&g_status.humidity, &g_status.temperature);

        delay(2000);
    }
}