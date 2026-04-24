#include "lcd.h"

static SemaphoreHandle_t lcd_mutex = NULL;

/* ========================= PRIVATE ========================= */

static void lcd_enable(void)
{
    ets_delay_us(1);
    gpio_set_level(LCD_E_PIN, 1);
    ets_delay_us(1);
    gpio_set_level(LCD_E_PIN, 0);
    ets_delay_us(50);
}

static void lcd_send_nibble(uint8_t data)
{
    gpio_set_level(LCD_D4_PIN, (data >> 0) & 1);
    gpio_set_level(LCD_D5_PIN, (data >> 1) & 1);
    gpio_set_level(LCD_D6_PIN, (data >> 2) & 1);
    gpio_set_level(LCD_D7_PIN, (data >> 3) & 1);
    lcd_enable();
}

static void _lcd_command(uint8_t cmd)
{
    gpio_set_level(LCD_RS_PIN, 0);
    lcd_send_nibble(cmd >> 4);
    lcd_send_nibble(cmd & 0x0F);
    ets_delay_us(37);
}

static void _lcd_send_data(uint8_t data)
{
    gpio_set_level(LCD_RS_PIN, 1);
    lcd_send_nibble(data >> 4);
    lcd_send_nibble(data & 0x0F);
    ets_delay_us(37);
}

/* ========================= INIT ========================= */

void lcd_init(void)
{
    lcd_mutex = xSemaphoreCreateMutex();

    esp_rom_gpio_pad_select_gpio(LCD_RS_PIN);
    esp_rom_gpio_pad_select_gpio(LCD_E_PIN);
    esp_rom_gpio_pad_select_gpio(LCD_D4_PIN);
    esp_rom_gpio_pad_select_gpio(LCD_D5_PIN);
    esp_rom_gpio_pad_select_gpio(LCD_D6_PIN);
    esp_rom_gpio_pad_select_gpio(LCD_D7_PIN);

    gpio_set_direction(LCD_RS_PIN, GPIO_MODE_OUTPUT);
    gpio_set_direction(LCD_E_PIN,  GPIO_MODE_OUTPUT);
    gpio_set_direction(LCD_D4_PIN, GPIO_MODE_OUTPUT);
    gpio_set_direction(LCD_D5_PIN, GPIO_MODE_OUTPUT);
    gpio_set_direction(LCD_D6_PIN, GPIO_MODE_OUTPUT);
    gpio_set_direction(LCD_D7_PIN, GPIO_MODE_OUTPUT);

    gpio_set_level(LCD_RS_PIN, 0);
    gpio_set_level(LCD_E_PIN,  0);

    ets_delay_us(50000);        // 50ms power-on

    lcd_send_nibble(0x03);
    ets_delay_us(5000);
    lcd_send_nibble(0x03);
    ets_delay_us(1000);
    lcd_send_nibble(0x03);
    ets_delay_us(1000);
    lcd_send_nibble(0x02);
    ets_delay_us(1000);

    _lcd_command(0x28);         // 4-bit, 2 lines, 5x8 font
    _lcd_command(0x08);         // display off
    _lcd_command(0x01);         // clear
    ets_delay_us(2000);
    _lcd_command(0x06);         // entry mode
    _lcd_command(0x0C);         // display on, no cursor
}

/* ========================= PUBLIC API ========================= */

void lcd_command(uint8_t cmd)
{
    if (xSemaphoreTake(lcd_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        _lcd_command(cmd);
        xSemaphoreGive(lcd_mutex);
    }
}

void lcd_set_cursor(uint8_t row, uint8_t col)
{
    uint8_t address = (row == 0) ? (0x80 + col) : (0xC0 + col);
    if (xSemaphoreTake(lcd_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        _lcd_command(address);
        xSemaphoreGive(lcd_mutex);
    }
}

void lcd_write_char(char c)
{
    if (xSemaphoreTake(lcd_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        _lcd_send_data(c);
        xSemaphoreGive(lcd_mutex);
    }
}

void lcd_write_string(char *str)
{
    if (xSemaphoreTake(lcd_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        while (*str) _lcd_send_data(*str++);
        xSemaphoreGive(lcd_mutex);
    }
}

void lcd_clear(void)
{
    if (xSemaphoreTake(lcd_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        _lcd_command(0x01);
        delay_ms(2);
        xSemaphoreGive(lcd_mutex);
    }
}