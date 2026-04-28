#include "flow_sensor.h"

static volatile uint32_t pulse_count[2] = {0, 0};

static void IRAM_ATTR isr_sensor1(void *arg) { pulse_count[0]++; }
static void IRAM_ATTR isr_sensor2(void *arg) { pulse_count[1]++; }

void init_flow_sensors(void)
{
    gpio_reset_pin(FLOW_SENS_1_PIN);
    gpio_reset_pin(FLOW_SENS_2_PIN);

    gpio_config_t cfg = {
        .pin_bit_mask = (1ULL << FLOW_SENS_1_PIN) | (1ULL << FLOW_SENS_2_PIN),
        .mode = GPIO_MODE_INPUT,
        .pull_up_en = GPIO_PULLUP_ENABLE,
        .intr_type = GPIO_INTR_POSEDGE,
    };
    gpio_config(&cfg);

    esp_err_t err = gpio_install_isr_service(0);

    if (err != ESP_OK && err != ESP_ERR_INVALID_STATE)
    {
        ESP_LOGE("FLOW", "ISR service install failed: %s", esp_err_to_name(err));
    }
    gpio_isr_handler_add(FLOW_SENS_1_PIN, isr_sensor1, NULL);
    gpio_isr_handler_add(FLOW_SENS_2_PIN, isr_sensor2, NULL);
}

void read_flow_sens(void)
{
    portDISABLE_INTERRUPTS();
    uint32_t c0 = pulse_count[0];
    pulse_count[0] = 0;
    uint32_t c1 = pulse_count[1];
    pulse_count[1] = 0;
    portENABLE_INTERRUPTS();

    g_state.flow_sens_1 = (c0 * 60000UL) / (PULSES_PER_LITER * SAMPLE_PERIOD);
    g_state.flow_sens_2 = (c1 * 60000UL) / (PULSES_PER_LITER * SAMPLE_PERIOD);
}