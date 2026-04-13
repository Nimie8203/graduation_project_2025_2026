#include "esp_idf_common.h"

bool is_wifi_on = false;

void app_main(void)
{
    blink_5_task();
    init_networking();
    is_wifi_on = true;

    while (1)
    {
        read_th(&g_status.humidity, &g_status.temperature);

        delay(2000);
    }
}