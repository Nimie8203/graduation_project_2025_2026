#ifndef TIMER_H
#define TIMER_H

#include "esp_idf_common.h"
#include <sys/time.h>
#include <time.h>
#include <stdint.h>
#include <stdbool.h>

// --- Configuration ---
#define TIMER_SYNC_MAX_DRIFT_MS 5000 // Accept sync if drift > 5s
#define TIMER_SYNC_FORCE_IF_MS 60000 // Force sync if drift > 60s

// --- Types ---
typedef struct
{
    int64_t unix_ms;             // Unix time in milliseconds from client
    int32_t timezone_offset_min; // Client's UTC offset in minutes (e.g. +180 for UTC+3)
} timer_sync_payload_t;

typedef struct
{
    bool is_synced;            // Has the clock been synced at least once?
    int64_t last_sync_unix_ms; // When was the last sync
    int32_t last_drift_ms;     // Drift detected at last sync
} timer_status_t;

void timer_init(void);
bool timer_sync_from_client(const timer_sync_payload_t *payload);
int64_t timer_get_unix_ms(void);
void timer_get_formatted(char *buf, size_t buflen);
timer_status_t timer_get_status(void);

#endif // TIMER_H