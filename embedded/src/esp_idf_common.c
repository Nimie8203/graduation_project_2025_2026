#include "esp_idf_common.h"

void delay(uint16_t time)
{
    vTaskDelay(time / portTICK_PERIOD_MS);
}