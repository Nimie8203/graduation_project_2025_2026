#include "tasks.h"
#include "t_h_sensor.h"

static void sensor_task(void *arg)
{
    while(1) {
        read_th(&g_state.humidity, &g_state.temperature);
        delay_ms(2000);
    }
}
static void irrigation_task(void *arg)
{
    while(1) {
        delay_ms(30000);
    }
}
void init_tasks(void)
{
    xTaskCreate(sensor_task,     "sensor_task",     3072, NULL, 5, NULL);
    xTaskCreate(irrigation_task, "irrigation_task", 3072, NULL, 5, NULL);
}