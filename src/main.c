#include "esp_idf_common.h"
#include "wifi_example.h"

void app_main()
{
    delay(2000);

    // PSUEDO-BOOT
    blink_5_task();
    delay(700);
    printf("\nBooted!\n");

    // Starting WIFI Example
    delay(1000);
    printf("\nStarting wifi example...");
    run_wifi_example();
    delay(1000);
    printf("Called wifi example!");


    // ENDING
    printf("\nTask Done\n");
}