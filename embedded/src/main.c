#include "esp_idf_common.h"

bool is_wifi_on = false;

void app_main(void)
{
    blink_5_task();
    init_networking();
    is_wifi_on = true;

    // Main loop — keeps app_main alive and feeds g_status
    // so HTTP handlers always have fresh data to return
    while (1)
    {
        read_th(&g_status.humidity, &g_status.temperature);

        // When you add more sensors, just call them here:
        // g_status.moisture = read_moisture();
        // g_status.lux      = read_light();
        // g_status.flow     = read_flow();

        delay(2000);  // read every 2 seconds
    }
}