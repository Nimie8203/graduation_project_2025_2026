#include "flow_sensor.h"

static volatile uint32_t pulse_count[2] = {0, 0};
static uint32_t *flow_rate_lpm[2] = {&g_state.flow_sens_1, &g_state.flow_sens_2};

static void IRAM_ATTR isr_sensor1(void *arg) { pulse_count[0]++; }
static void IRAM_ATTR isr_sensor2(void *arg) { pulse_count[1]++; }

void init_flow_sensors(void)
{
    gpio_config_t cfg = {
        .pin_bit_mask = (1ULL << FLOW_SENS_1_PIN) | (1ULL << FLOW_SENS_2_PIN),
        .mode = GPIO_MODE_INPUT,
        .pull_up_en = GPIO_PULLUP_ENABLE,
        .intr_type = GPIO_INTR_POSEDGE,
    };
    gpio_config(&cfg);

    gpio_install_isr_service(0);

    gpio_isr_handler_add(FLOW_SENS_1_PIN, isr_sensor1, NULL);
    gpio_isr_handler_add(FLOW_SENS_2_PIN, isr_sensor2, NULL);
}

void read_flow_sens()
{
    *flow_rate_lpm[0] = (uint32_t) (pulse_count[0] / 7.5);
    *flow_rate_lpm[1] = (uint32_t) (pulse_count[1] / 7.5);
}