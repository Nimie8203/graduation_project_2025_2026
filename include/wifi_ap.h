#ifndef WIFI_AP_H
#define WIFI_AP_H

#include "esp_http_server.h"

void wifi_ap_start(void);
void wifi_init_ap(void);
void start_webserver(void);
esp_err_t api_led_on(httpd_req_t *req);
esp_err_t api_led_off(httpd_req_t *req);
esp_err_t api_led_status(httpd_req_t *req);
esp_err_t api_led_blink(httpd_req_t *req);

#endif