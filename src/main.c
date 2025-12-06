#include "esp_idf_common.h"
#
// --------------------------------------------------
// MAIN
// --------------------------------------------------
void app_main(void)
{
    delay(2000);
    blink_5_task();
    printf("Booted!");
    delay(1000);

    printf("Initializing networking...");
    init_networking();

    printf("Task done...");
    delay(2000);
}
