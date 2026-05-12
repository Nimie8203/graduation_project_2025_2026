#include "esp_idf_common.h"
#include "led.h"
#include "networking.h"
#include "lcd.h"
#include "t_h_sensor.h"
#include "pump.h"
#include "flow_sensor.h"
#include "moisture_sensor.h"
#include "ldr_sensor.h"
#include "tasks.h"
#include "uart.h"

uint8_t line_1_avg = 0;
uint8_t line_2_avg = 0;

const uint8_t OFFSET_HOT_DRY   = 20;
const uint8_t OFFSET_HOT_HUMID =  8;
const uint8_t OFFSET_COLD_DRY  =  5;
const uint8_t OFFSET_COLD_WET  = 15;
int16_t irrig_amount = 0;

void app_main(void)
{
    delay_ms(5000);
    init_monitoring_uart();
    delay_ms(1000);
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
    ESP_LOGI(GENERAL_TAG, "Initializing Devices");
    init_led();
    init_lcd();
    init_networking();
    init_adc1_shared();
    init_ldr();
    init_moisture();
    init_pumps();
    init_flow_sensors();
    init_states();
    init_tasks();
    blink_5();
    ESP_LOGI(GENERAL_TAG, "BOOTED");
    lcd_write_string("BOOTED!");
    delay_ms(2000);
    lcd_clear();
    delay_ms(10);
    lcd_set_cursor(0, 0);
    lcd_write_string("READING STATES...");
    delay_ms(2000);
    lcd_clear();
    delay_ms(10);

    // Initialize to moist_upper so no pump fires on the very first
    // iteration before any weather condition has been evaluated.
    irrig_amount = g_state.profile.moist_upper;

    while (1)
    {
        delay_ms(2000);
        ESP_LOGI(TEMP_TAG,    "%d", g_state.temperature);
        ESP_LOGI(HUM_TAG,     "%d", g_state.humidity);
        ESP_LOGI(FLOW_TAG,    "%d", g_state.flow_sens_1);
        ESP_LOGI(FLOW_TAG,    "%d", g_state.flow_sens_2);
        ESP_LOGI(LDR_TAG,     "%d", g_state.light_intensity);
        ESP_LOGI(MOIST_TAG,   "%d", g_state.moisture_1);
        ESP_LOGI(MOIST_TAG,   "%d", g_state.moisture_2);
        ESP_LOGI(MOIST_TAG,   "%d", g_state.moisture_3);
        ESP_LOGI(MOIST_TAG,   "%d", g_state.moisture_4);
        ESP_LOGI(PUMP_TAG,    "%d", g_state.pump_1_state);
        ESP_LOGI(PUMP_TAG,    "%d", g_state.pump_2_state);
        ESP_LOGI(PUMP_TAG,    "%d", g_state.pump_1_triggered);
        ESP_LOGI(PUMP_TAG,    "%d", g_state.pump_2_triggered);
        ESP_LOGI(TANK_TAG,    "%d", g_state.tank_state);
        ESP_LOGI(PIPE_TAG,    "%d", g_state.pipe_state);
        ESP_LOGI(IRRIG_TAG,   "irrig_amount=%d", irrig_amount);
        ESP_LOGI(GENERAL_TAG, "++++++++++++++");

        line_1_avg = (uint8_t)(g_state.moisture_1 + g_state.moisture_2) / 2;
        line_2_avg = (uint8_t)(g_state.moisture_3 + g_state.moisture_4) / 2;

        // ── STOP CONDITION ───────────────────────────────────────────────
        // irrig_amount persists from the previous iteration so this always
        // checks against the target that was actually set for this cycle.
        if (irrig_amount <= line_1_avg && irrig_amount <= line_2_avg)
        {
            pump_off(1);
            pump_off(2);
            g_state.pump_1_triggered = false;
            g_state.pump_2_triggered = false;
        }
        else if (irrig_amount <= line_1_avg)
        {
            pump_off(1);
            g_state.pump_1_triggered = false;
        }
        else if (irrig_amount <= line_2_avg)
        {
            pump_off(2);
            g_state.pump_2_triggered = false;
        }

        // ── DAYLIGHT BLOCK ───────────────────────────────────────────────
        if (g_state.light_intensity > g_state.profile.light_threshold)
        {
            pump_off(1);
            pump_off(2);
            g_state.pump_1_triggered = false;
            g_state.pump_2_triggered = false;
            continue;
        }

        // ── WEATHER-BASED IRRIGATION TARGET + TRIGGER ────────────────────
        //
        // Hot & dry  → water more than usual  (moist_upper + OFFSET_HOT_DRY)
        // Hot & humid→ water slightly more    (moist_upper + OFFSET_HOT_HUMID)
        // Cold & dry → water a little less    (moist_upper - OFFSET_COLD_DRY)
        // Cold & wet → water noticeably less  (moist_upper - OFFSET_COLD_WET)
        // Normal     → water to moist_upper
        //
        // In every case the pump only triggers when the line average has
        // fallen to or below moist_lower (the "too dry" threshold).

        if (g_state.temperature >= g_state.profile.temp_upper &&
            g_state.humidity    <= g_state.profile.hum_lower)
        {
            // Hot & dry: highest irrigation target
            irrig_amount = g_state.profile.moist_upper + OFFSET_HOT_DRY;
            if (irrig_amount > 100) irrig_amount = 100;

            if (line_1_avg <= g_state.profile.moist_lower &&
                line_2_avg <= g_state.profile.moist_lower)
            {
                g_state.pump_1_triggered = true;
                g_state.pump_2_triggered = true;
            }
            else if (line_1_avg <= g_state.profile.moist_lower)
                g_state.pump_1_triggered = true;
            else if (line_2_avg <= g_state.profile.moist_lower)
                g_state.pump_2_triggered = true;
        }
        else if (g_state.temperature >= g_state.profile.temp_upper &&
                 g_state.humidity    >= g_state.profile.hum_upper)
        {
            // Hot & humid: slightly elevated target (humidity slows evaporation)
            irrig_amount = g_state.profile.moist_upper + OFFSET_HOT_HUMID;
            if (irrig_amount > 100) irrig_amount = 100;

            if (line_1_avg <= g_state.profile.moist_lower &&
                line_2_avg <= g_state.profile.moist_lower)
            {
                g_state.pump_1_triggered = true;
                g_state.pump_2_triggered = true;
            }
            else if (line_1_avg <= g_state.profile.moist_lower)
                g_state.pump_1_triggered = true;
            else if (line_2_avg <= g_state.profile.moist_lower)
                g_state.pump_2_triggered = true;
        }
        else if (g_state.temperature <= g_state.profile.temp_lower &&
                 g_state.humidity    <= g_state.profile.hum_lower)
        {
            // Cold & dry: water a little less than normal.
            // Subtract from moist_upper (the target), NOT from moist_lower
            // (the trigger). The plant still gets watered up to a level, just
            // a lower one than in normal conditions.
            irrig_amount = g_state.profile.moist_upper - OFFSET_COLD_DRY;

            if (line_1_avg <= g_state.profile.moist_lower &&
                line_2_avg <= g_state.profile.moist_lower)
            {
                g_state.pump_1_triggered = true;
                g_state.pump_2_triggered = true;
            }
            else if (line_1_avg <= g_state.profile.moist_lower)
                g_state.pump_1_triggered = true;
            else if (line_2_avg <= g_state.profile.moist_lower)
                g_state.pump_2_triggered = true;
        }
        else if (g_state.temperature <= g_state.profile.temp_lower &&
                 g_state.humidity    >= g_state.profile.hum_upper)
        {
            // Cold & wet: water the least (plant barely loses moisture at all)
            irrig_amount = g_state.profile.moist_upper - OFFSET_COLD_WET;

            if (line_1_avg <= g_state.profile.moist_lower &&
                line_2_avg <= g_state.profile.moist_lower)
            {
                g_state.pump_1_triggered = true;
                g_state.pump_2_triggered = true;
            }
            else if (line_1_avg <= g_state.profile.moist_lower)
                g_state.pump_1_triggered = true;
            else if (line_2_avg <= g_state.profile.moist_lower)
                g_state.pump_2_triggered = true;
        }
        else
        {
            // Normal conditions: water to moist_upper
            irrig_amount = g_state.profile.moist_upper;
            if (irrig_amount > 100) irrig_amount = 100;

            if (line_1_avg <= g_state.profile.moist_lower &&
                line_2_avg <= g_state.profile.moist_lower)
            {
                g_state.pump_1_triggered = true;
                g_state.pump_2_triggered = true;
            }
            else if (line_1_avg <= g_state.profile.moist_lower)
                g_state.pump_1_triggered = true;
            else if (line_2_avg <= g_state.profile.moist_lower)
                g_state.pump_2_triggered = true;
        }

        // ── ACTUATE PUMPS ────────────────────────────────────────────────
        if (g_state.pump_1_triggered && g_state.pump_2_triggered)
        {
            pump_on(1);
            pump_on(2);
        }
        else if (g_state.pump_1_triggered)
        {
            pump_on(1);
        }
        else if (g_state.pump_2_triggered)
        {
            pump_on(2);
        }
    }
}