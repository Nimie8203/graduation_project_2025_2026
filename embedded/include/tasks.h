#ifndef TASKS_H
#define TASKS_H

#include "esp_idf_common.h"

#include "t_h_sensor.h"
#include "flow_sensor.h"
#include "led.h"
#include "lcd.h"
#include "moisture_sensor.h"
#include "ldr_sensor.h"
#include "tank.h"
#include "pipe.h"

void init_tasks(void);

#endif