#include "esp_idf_common.h"

bool is_wifi_on = false;

void app_main(void)
{
    delay(2000);
    blink_5_task();
    printf("Booted!\n");
    delay(1000);

    on_press();

    printf("Initializing networking...\n");
    delay(2000);
    init_networking();
    delay(2000);
    is_wifi_on = true;
    //blink_wifi(is_wifi_on);

    printf("Task done...\n");
    delay(2000);
}
