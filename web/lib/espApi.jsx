const BASE_URL = 'http://192.168.4.1';
const DEFAULT_TIMEOUT = 5000; // 5 seconds

/**
 * Makes a fetch request with timeout and error handling
 * @param {string} endpoint - The API endpoint to call
 * @param {RequestInit} options - Fetch options (method, body, etc.)
 * @param {number} timeout - Request timeout in milliseconds
 * @returns {Promise<any>} - Parsed JSON response
 */
async function fetchWithTimeout(endpoint, options = {}, timeout = DEFAULT_TIMEOUT) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      ...options,
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    clearTimeout(timeoutId);
    
    if (error.name === 'AbortError') {
      throw new Error('Request timeout: ESP32 device did not respond in time');
    }
    
    if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
      throw new Error('Connection error: Unable to reach ESP32 device');
    }
    
    throw error;
  }
}

export async function getSensors() {
  return fetchWithTimeout('/api/status', {
    method: 'GET',
  });
}

/**
 * Starts the pump on ESP32
 * @returns {Promise<Object>} - Response from ESP32
 */
export async function startPump() {
  return pump1On();
}

/**
 * Stops the pump on ESP32
 * @returns {Promise<Object>} - Response from ESP32
 */
export async function stopPump() {
  return pump1Off();
}

/**
 * Sends plant profile to ESP32
 * @param {Object} profile - Plant profile object
 * @returns {Promise<Object>} - Response from ESP32
 */
export async function sendPlantProfile(profile) {
  return createProfile(profile);
}

export async function getStatus() {
  return fetchWithTimeout('/api/status', { method: 'GET' });
}

export async function setLed(on) {
  return fetchWithTimeout('/api/led', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ state: on ? 'on' : 'off' }),
  });
}

export const ledOn = () => setLed(true);
export const ledOff = () => setLed(false);

export async function setPump(pumpNum, on) {
  if (pumpNum !== 1 && pumpNum !== 2) {
    throw new Error('pumpNum must be 1 or 2');
  }

  return fetchWithTimeout('/api/pump', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ pump: pumpNum, state: on ? 'on' : 'off' }),
  });
}

export const pump1On = () => setPump(1, true);
export const pump1Off = () => setPump(1, false);
export const pump2On = () => setPump(2, true);
export const pump2Off = () => setPump(2, false);

export async function createProfile(profile) {
  return fetchWithTimeout('/api/profile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(profile),
  });
}

export async function getProfile() {
  return fetchWithTimeout('/api/profile', { method: 'GET' });
}

export async function deleteProfile() {
  return fetchWithTimeout('/api/profile', { method: 'DELETE' });
}

// Backward-compatible aliases used by current pages.
export async function runLedOn() { return ledOn(); }
export async function runLedOff() { return ledOff(); }
export async function runPumpOn() { return pump1On(); }
export async function runPumpOff() { return pump1Off(); }
export async function runStatusGeneral() { return getStatus(); }
export async function runProfile(profile = {}) { return createProfile(profile); }

