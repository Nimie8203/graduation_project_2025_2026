#include "lcd.h"

/* =========================
   PRIVATE FUNCTIONS
   ========================= */

static void lcd_enable(void)
{
    ets_delay_us(1);              // data setup time before rising edge
    gpio_set_level(LCD_E_PIN, 1);
    ets_delay_us(1);              // E pulse width — spec says ≥450ns, 1µs is safe
    gpio_set_level(LCD_E_PIN, 0);
    ets_delay_us(50);             // execution time — LCD needs ~37-50µs to process nibble
}

static void lcd_send_nibble(uint8_t data)
{
    gpio_set_level(LCD_D4_PIN, (data >> 0) & 1);
    gpio_set_level(LCD_D5_PIN, (data >> 1) & 1);
    gpio_set_level(LCD_D6_PIN, (data >> 2) & 1);
    gpio_set_level(LCD_D7_PIN, (data >> 3) & 1);
    lcd_enable();
}

/* =========================
   LOW-LEVEL FUNCTIONS
   ========================= */

void lcd_command(uint8_t cmd)
{
    gpio_set_level(LCD_RS_PIN, 0);   // command mode
    lcd_send_nibble(cmd >> 4);
    lcd_send_nibble(cmd & 0x0F);
    ets_delay_us(37);                // most commands need ~37µs
}

void lcd_send_data(uint8_t data)
{
    gpio_set_level(LCD_RS_PIN, 1);   // data mode
    lcd_send_nibble(data >> 4);
    lcd_send_nibble(data & 0x0F);
    ets_delay_us(37);
}

/* =========================
   INITIALIZATION
   ========================= */

void lcd_init(void)
{
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

    ets_delay_us(50000);  // 50ms power-on wait

    gpio_set_level(LCD_RS_PIN, 0);
    lcd_send_nibble(0x03);
    ets_delay_us(5000);   // 5ms

    lcd_send_nibble(0x03);
    ets_delay_us(1000);   // 1ms

    lcd_send_nibble(0x03);
    ets_delay_us(1000);

    lcd_send_nibble(0x02);
    ets_delay_us(1000);

    lcd_command(0x28);
    lcd_command(0x08);
    lcd_command(0x01);
    ets_delay_us(2000);   // 2ms for clear
    lcd_command(0x06);
    lcd_command(0x0C);
    lcd_clear();
}

/* =========================
   HIGH-LEVEL FUNCTIONS
   ========================= */

void lcd_write_char(char c)
{
    lcd_send_data(c);
}

void lcd_write_string(char *str)
{
    while (*str)
    {
        lcd_write_char(*str);
        str++;
    }
}

void lcd_set_cursor(uint8_t row, uint8_t col)
{
    uint8_t address;
    if (row == 0)
        address = 0x80 + col;
    else
        address = 0xC0 + col;
    lcd_command(address);
}

void lcd_clear(void)
{
    lcd_command(0x01);
    delay_ms(2);   // clear is the slowest command — 1.52ms minimum
}