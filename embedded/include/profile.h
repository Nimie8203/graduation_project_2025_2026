#ifndef PROFILE_H
#define PROFILE_H

#include "esp_idf_common.h" // This has all of the needed includes for espidf

#define MAX_NAME_LENGTH 32
#define MAX_IRRIG_TIME_PER_DAY 24
#define MAX_PROFILES 10
#define NVS_NAMESPACE "profiles"

typedef struct
{
    uint8_t id;
    char profile_name[MAX_NAME_LENGTH];
    char plant_name[MAX_NAME_LENGTH];
    uint8_t irrig_times_per_day;
    uint16_t times_of_day[MAX_IRRIG_TIME_PER_DAY]; // UNIT IS MINUTES FROM MIDNIGHT
    uint16_t water_amount_per_irrig;
    uint8_t moisture_threshold;
    // COULD ADD MORE CONDITIONAL STUFF LIKE ONLY WATER WHEN TEMP IS ABOVE 0 OR SOMETHING LIKE THAT
} profile_t;

esp_err_t add_profile(profile_t profile);
esp_err_t edit_profile(profile_t updated_profile);   // match by updated_profile.id
esp_err_t remove_profile(uint8_t id);
esp_err_t remove_all_profiles(void);

esp_err_t load_profile(uint8_t id, profile_t *out_profile);
esp_err_t load_all_profiles(profile_t *out_array, uint8_t *out_count);
#endif