#include "esp_idf_common.h"

void app_main(void)
{
    delay(2000);
    blink_5_task();
    printf("Booted!");
    delay(1000);

    printf("Initializing networking...");
    delay(2000);
    init_networking();
    delay(2000);

    printf("Task done...");
    delay(2000);
}
