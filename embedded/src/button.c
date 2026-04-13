#include "button.h"

void on_press(){

    // Configure button GPIO as input
    gpio_set_direction(BUTTON_PIN, GPIO_MODE_INPUT);
    gpio_set_pull_mode(BUTTON_PIN, GPIO_PULLUP_ONLY);


    while(1) {
        int button_state = gpio_get_level(BUTTON_PIN);
        
        if (button_state == 0) {
            // Button pressed - turn LED ON
            led_on();
        } else {
            // Button released - turn LED OFF
            led_off();
        }
        
        // Small delay to debounce and reduce CPU usage
        delay(50);
    }
}