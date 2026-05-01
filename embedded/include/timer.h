#ifndef TIMER_H
#define TIMER_H

#include "esp_idf_common.h"

// --- Configuration ---
#define TIMER_SYNC_MAX_DRIFT_MS 5000 // Accept sync if drift > 5s
#define TIMER_SYNC_FORCE_IF_MS 60000 // Force sync if drift > 60s
#define TIMER_FALLBACK_YEAR   2025
#define TIMER_FALLBACK_MONTH  6      // 1-12
#define TIMER_FALLBACK_DAY    1
#define TIMER_FALLBACK_HOUR   8      // 24h, local time
#define TIMER_FALLBACK_MIN    0
#define TIMER_FALLBACK_TZ_MIN 180    // UTC+3 (Turkey), in minutes

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

void init_timer(void);
bool timer_sync_from_client(const timer_sync_payload_t *payload);
int64_t timer_get_unix_ms(void);
void timer_get_formatted(char *buf, size_t buflen);
timer_status_t timer_get_status(void);

#endif