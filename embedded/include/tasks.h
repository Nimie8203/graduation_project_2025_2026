#ifndef TASKS_H
#define TASKS_H

#include "esp_idf_common.h"

static void sensor_task(void *arg);
static void irrigation_task(void *arg);
void init_tasks(void);

#endif