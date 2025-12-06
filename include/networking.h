#ifndef NETWORKING_H
#define NETWORKING_H

#include "esp_idf_common.h"

void give_command(int val);
esp_err_t api_cmd_handler(httpd_req_t *req);
httpd_handle_t start_webserver(void);
void wifi_init_softap(void);
void init_networking(void);


#endif