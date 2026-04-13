#ifndef LCD_H  // check if this file was included before
#define LCD_H  //Marks this file as already included , with #ifndef LCD_H they are include guard

#include "esp_idf_common.h" //ESP system stuff
#include <stdint.h> // standard data types (im worried of it,it gave me falshbacks * _ *  )


/* 1. Initialization */
void lcd_init(void);   //Prepares the LCD to work


/* 2. Configuration */
void lcd_command(uint8_t cmd);  // Sends a command to LCD , like turn display on/ clear screen / move Move cursor 
void lcd_send_data(uint8_t data); // Sends data (characters) 


/* 3. User functions */
void lcd_write_char(char c); // Displays one character
void lcd_write_string(char *str); // Displays full text
void lcd_set_cursor(uint8_t row, uint8_t col); // Moves cursor position
void lcd_clear(void);  //Clears the screen

#endif