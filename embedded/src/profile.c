#include "profile.h"

static const char *TAG = "profile";

// Builds the NVS key for a given profile id, e.g. "p_3"
static void make_key(uint8_t id, char *key_buf, size_t buf_size)
{
    snprintf(key_buf, buf_size, "p_%u", id);
}

// -----------------------------------------------------------------------
// Internal helpers for the ID index
// The index is a flat array of uint8_t IDs stored under key "index".
// -----------------------------------------------------------------------

static esp_err_t read_index(nvs_handle_t handle, uint8_t *ids, uint8_t *count)
{
    size_t required = MAX_PROFILES * sizeof(uint8_t);
    uint8_t buf[MAX_PROFILES];
    esp_err_t err = nvs_get_blob(handle, "index", buf, &required);

    if (err == ESP_ERR_NVS_NOT_FOUND) {
        *count = 0;
        return ESP_OK;
    }
    if (err != ESP_OK) return err;

    *count = (uint8_t)(required / sizeof(uint8_t));
    memcpy(ids, buf, required);
    return ESP_OK;
}

static esp_err_t write_index(nvs_handle_t handle, const uint8_t *ids, uint8_t count)
{
    return nvs_set_blob(handle, "index", ids, count * sizeof(uint8_t));
}

// -----------------------------------------------------------------------
// Public API
// -----------------------------------------------------------------------

esp_err_t add_profile(profile_t profile)
{
    nvs_handle_t handle;
    esp_err_t err;

    err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &handle);
    if (err != ESP_OK) return err;

    // Load current index
    uint8_t ids[MAX_PROFILES];
    uint8_t count = 0;
    err = read_index(handle, ids, &count);
    if (err != ESP_OK) goto done;

    if (count >= MAX_PROFILES) {
        ESP_LOGE(TAG, "add_profile: max profiles (%d) reached", MAX_PROFILES);
        err = ESP_ERR_NO_MEM;
        goto done;
    }

    // Check for duplicate ID
    for (uint8_t i = 0; i < count; i++) {
        if (ids[i] == profile.id) {
            ESP_LOGE(TAG, "add_profile: id %u already exists", profile.id);
            err = ESP_ERR_INVALID_ARG;
            goto done;
        }
    }

    // Write the profile blob
    char key[16];
    make_key(profile.id, key, sizeof(key));
    err = nvs_set_blob(handle, key, &profile, sizeof(profile_t));
    if (err != ESP_OK) goto done;

    // Update index
    ids[count] = profile.id;
    count++;
    err = write_index(handle, ids, count);
    if (err != ESP_OK) goto done;

    err = nvs_commit(handle);
    ESP_LOGI(TAG, "add_profile: saved profile id=%u ('%s')", profile.id, profile.profile_name);

done:
    nvs_close(handle);
    return err;
}

esp_err_t edit_profile(profile_t updated_profile)
{
    nvs_handle_t handle;
    esp_err_t err;

    err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &handle);
    if (err != ESP_OK) return err;

    // Verify the id exists in the index
    uint8_t ids[MAX_PROFILES];
    uint8_t count = 0;
    err = read_index(handle, ids, &count);
    if (err != ESP_OK) goto done;

    bool found = false;
    for (uint8_t i = 0; i < count; i++) {
        if (ids[i] == updated_profile.id) { found = true; break; }
    }

    if (!found) {
        ESP_LOGE(TAG, "edit_profile: id %u not found", updated_profile.id);
        err = ESP_ERR_NOT_FOUND;
        goto done;
    }

    // Overwrite the blob — key stays the same
    char key[16];
    make_key(updated_profile.id, key, sizeof(key));
    err = nvs_set_blob(handle, key, &updated_profile, sizeof(profile_t));
    if (err != ESP_OK) goto done;

    err = nvs_commit(handle);
    ESP_LOGI(TAG, "edit_profile: updated profile id=%u", updated_profile.id);

done:
    nvs_close(handle);
    return err;
}

esp_err_t remove_profile(uint8_t id)
{
    nvs_handle_t handle;
    esp_err_t err;

    err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &handle);
    if (err != ESP_OK) return err;

    uint8_t ids[MAX_PROFILES];
    uint8_t count = 0;
    err = read_index(handle, ids, &count);
    if (err != ESP_OK) goto done;

    // Find and remove from index
    bool found = false;
    for (uint8_t i = 0; i < count; i++) {
        if (ids[i] == id) {
            // Shift remaining IDs left
            memmove(&ids[i], &ids[i + 1], (count - i - 1) * sizeof(uint8_t));
            count--;
            found = true;
            break;
        }
    }

    if (!found) {
        ESP_LOGW(TAG, "remove_profile: id %u not found", id);
        err = ESP_ERR_NOT_FOUND;
        goto done;
    }

    // Erase the profile blob
    char key[16];
    make_key(id, key, sizeof(key));
    err = nvs_erase_key(handle, key);
    if (err != ESP_OK) goto done;

    // Save updated index
    err = write_index(handle, ids, count);
    if (err != ESP_OK) goto done;

    err = nvs_commit(handle);
    ESP_LOGI(TAG, "remove_profile: removed id=%u", id);

done:
    nvs_close(handle);
    return err;
}

esp_err_t remove_all_profiles(void)
{
    nvs_handle_t handle;
    esp_err_t err;

    err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &handle);
    if (err != ESP_OK) return err;

    // Erase every key in this namespace (cleanest approach)
    err = nvs_erase_all(handle);
    if (err != ESP_OK) goto done;

    err = nvs_commit(handle);
    ESP_LOGI(TAG, "remove_all_profiles: namespace cleared");

done:
    nvs_close(handle);
    return err;
}

esp_err_t load_profile(uint8_t id, profile_t *out_profile)
{
    nvs_handle_t handle;
    esp_err_t err;

    err = nvs_open(NVS_NAMESPACE, NVS_READONLY, &handle);
    if (err != ESP_OK) return err;

    char key[16];
    make_key(id, key, sizeof(key));
    size_t size = sizeof(profile_t);
    err = nvs_get_blob(handle, key, out_profile, &size);

    nvs_close(handle);
    return err;
}

esp_err_t load_all_profiles(profile_t *out_array, uint8_t *out_count)
{
    nvs_handle_t handle;
    esp_err_t err;

    err = nvs_open(NVS_NAMESPACE, NVS_READONLY, &handle);
    if (err != ESP_OK) return err;

    uint8_t ids[MAX_PROFILES];
    uint8_t count = 0;
    err = read_index(handle, ids, &count);
    if (err != ESP_OK) goto done;

    for (uint8_t i = 0; i < count; i++) {
        char key[16];
        make_key(ids[i], key, sizeof(key));
        size_t size = sizeof(profile_t);
        err = nvs_get_blob(handle, key, &out_array[i], &size);
        if (err != ESP_OK) goto done;
    }

    *out_count = count;

done:
    nvs_close(handle);
    return err;
}