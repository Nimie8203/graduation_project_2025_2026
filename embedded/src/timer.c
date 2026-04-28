#include "timer.h"

static bool s_is_synced = false;
static int64_t s_last_sync_unix_ms = 0;
static int32_t s_last_drift_ms = 0;
static int32_t s_timezone_offset_min = 0; // Default UTC

void timer_init(void)
{
    // Set to a known zero epoch; clock will drift until first sync
    struct timeval tv = {.tv_sec = 0, .tv_usec = 0};
    settimeofday(&tv, NULL);
    s_is_synced = false;
    ESP_LOGI(TIMER_TAG, "Timer initialized (awaiting client sync)");
}

int64_t timer_get_unix_ms(void)
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (int64_t)tv.tv_sec * 1000LL + (int64_t)(tv.tv_usec / 1000);
}

bool timer_sync_from_client(const timer_sync_payload_t *payload)
{
    if (!payload || payload->unix_ms <= 0)
    {
        ESP_LOGW(TIMER_TAG, "Sync rejected: invalid payload");
        return false;
    }

    int64_t current_ms = timer_get_unix_ms();
    int32_t drift_ms = (int32_t)(payload->unix_ms - current_ms);
    int32_t abs_drift = drift_ms < 0 ? -drift_ms : drift_ms;

    ESP_LOGI(TIMER_TAG, "Sync request: client=%lld esp=%lld drift=%d ms",
             payload->unix_ms, current_ms, drift_ms);

    // If not yet synced, always accept
    // If drift exceeds threshold, accept
    // If drift is small, skip (clock is good enough)
    if (s_is_synced && abs_drift < TIMER_SYNC_MAX_DRIFT_MS)
    {
        ESP_LOGI(TIMER_TAG, "Sync skipped: drift %d ms within tolerance", drift_ms);
        return false;
    }

    // Apply the new time
    struct timeval tv;
    tv.tv_sec = (time_t)(payload->unix_ms / 1000);
    tv.tv_usec = (suseconds_t)((payload->unix_ms % 1000) * 1000);
    settimeofday(&tv, NULL);

    // Update timezone and state
    s_timezone_offset_min = payload->timezone_offset_min;
    s_last_drift_ms = drift_ms;
    s_last_sync_unix_ms = payload->unix_ms;
    s_is_synced = true;

    ESP_LOGI(TIMER_TAG, "Clock synced. Drift was %d ms, TZ offset %d min",
             drift_ms, s_timezone_offset_min);
    return true;
}

void timer_get_formatted(char *buf, size_t buflen)
{
    int64_t unix_ms = timer_get_unix_ms();
    // Shift by timezone offset
    time_t local_sec = (time_t)(unix_ms / 1000) + (time_t)(s_timezone_offset_min * 60);
    struct tm t;
    gmtime_r(&local_sec, &t); // use gmtime_r on the already-shifted value

    snprintf(buf, buflen, "%04d-%02d-%02d %02d:%02d:%02d (UTC%+ld:%02d)",
             t.tm_year + 1900, t.tm_mon + 1, t.tm_mday,
             t.tm_hour, t.tm_min, t.tm_sec,
             s_timezone_offset_min / 60,
             abs(s_timezone_offset_min % 60));
}

timer_status_t timer_get_status(void)
{
    return (timer_status_t){
        .is_synced = s_is_synced,
        .last_sync_unix_ms = s_last_sync_unix_ms,
        .last_drift_ms = s_last_drift_ms,
    };
}