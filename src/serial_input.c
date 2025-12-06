#include "esp_idf_common.h"

void get_serial_input(char *buffer, size_t size)
{
    uint8_t index = 0; // Current index in buffer

    printf("\nType then press enter:\n");
    while (1)
    {

        int received_char = getchar();
        // Reason for int is for the errors. Errors are -1. char cant take -1.

        if (received_char > 0)
        {
            if (received_char == '\n' && index == 0)
            {
                printf("\nNothing was entered!\n");
                continue; // If empty string was entered, ignore
            }

            if (received_char == '\n') // if enter key was pressed
            {
                buffer[index] = '\0'; // Insert null character "\0" to the current index where enter was pressed.

                // Convert to lowercase for easy compare
                for (int i = 0; buffer[i]; i++)
                    buffer[i] = tolower(buffer[i]);
                index = 0;
                return;
            }
            else if (index < size - 1)
            {
                buffer[index++] = received_char;// Add the character to the buffer
                putchar(received_char);  
            }
        }

        delay(50);
    }
}