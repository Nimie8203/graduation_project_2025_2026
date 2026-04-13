#include "lcd.h" //Includes your header file function declarations

/* =========================
   GPIO PIN DEFINITIONS
   ========================= */
#define RS  2  //select command
#define EN  4  //enable signal
#define D4  16 //data lines (4-bit mode),from d4 to d7
#define D5  17
#define D6  18
#define D7  19

/* =========================
   PRIVATE FUNCTIONS
   ========================= */

static void lcd_delay(void) {  //Small delay so LCD can process data
    for(int i = 0; i < 5000; i++);
}

static void lcd_enable(void) {
    gpio_set_level(EN, 1);  //Turns EN ON then OFF, this will tell the lcd read the data now
    lcd_delay();
    gpio_set_level(EN, 0);
}

static void lcd_send_nibble(uint8_t data) { //Sends 4 bits only (because LCD is in 4-bit mode)
    gpio_set_level(D4, (data >> 0) & 1); //Send bit 0 to pin D4
    gpio_set_level(D5, (data >> 1) & 1); //Send bit 1 to D5
    gpio_set_level(D6, (data >> 2) & 1); //Send bit 2 to D6
    gpio_set_level(D7, (data >> 3) & 1); //Send bit 3 to D7
    lcd_enable();  //Tell LCD to read these bits
}

/* =========================
   LOW-LEVEL FUNCTIONS
   ========================= */

void lcd_command(uint8_t cmd) { //Sends instruction to LCD

    gpio_set_level(RS, 0); //RS = 0 → command mode

    lcd_send_nibble(cmd >> 4); //Send upper 4 bits
    lcd_send_nibble(cmd & 0x0F); //Send lower 4 bits

    lcd_delay(); //Wait for LCD
}

void lcd_send_data(uint8_t data) { //Sends a character

    gpio_set_level(RS, 1);  //RS = 1 → data mode

    lcd_send_nibble(data >> 4); //Send character in 2 parts line 66 & 67
    lcd_send_nibble(data & 0x0F); 

    lcd_delay(); //waiting
}

/* =========================
   INITIALIZATION
   ========================= */

void lcd_init(void) {  //Starts LCD

    gpio_set_direction(RS, GPIO_MODE_OUTPUT); //Set all pins as output
    gpio_set_direction(EN, GPIO_MODE_OUTPUT);
    gpio_set_direction(D4, GPIO_MODE_OUTPUT);
    gpio_set_direction(D5, GPIO_MODE_OUTPUT);
    gpio_set_direction(D6, GPIO_MODE_OUTPUT);
    gpio_set_direction(D7, GPIO_MODE_OUTPUT);

    lcd_delay(); //Initial wait

    lcd_command(0x28); //Set 4-bit mode, 2 lines
    lcd_command(0x0C); //Display ON, cursor OFF
    lcd_command(0x06); //Auto move cursor right
    lcd_clear();   // use high-level instead of repeating 0x01 ,,Clear screen
}

/* =========================
   HIGH-LEVEL FUNCTIONS
   ========================= */

void lcd_write_char(char c) { //Sends one character
    lcd_send_data(c);
}

void lcd_write_string(char *str) {  //Loop through text
    while(*str) { //Until end of string
        lcd_write_char(*str); //Print each character
        str++; //Move to next character
    }
}

void lcd_set_cursor(uint8_t row, uint8_t col) { //Move cursor position

    uint8_t address;

    if(row == 0) //First row
        address = 0x80 + col;
    else //Second row
        address = 0xC0 + col;

    lcd_command(address);  // uses low-level //Send position to LCD
}

void lcd_clear(void) {
    lcd_command(0x01);  // uses low-level, Clears display

}