const BASE_URL = 'http://192.168.4.1';
const DEFAULT_TIMEOUT = 5000; // 5 seconds
const COMMAND_ENDPOINT = '/command';

export const ESP32_COMMANDS = Object.freeze({
  LED_ON: 'LED_ON',
  LED_OFF: 'LED_OFF',
  PUMP_ON: 'PUMP_ON',
  PUMP_OFF: 'PUMP_OFF',
  LED_READ: 'LED_READ',
  TEMP_READ: 'TEMP_READ',
  HUMIDITY_READ: 'HUMIDITY_READ',
  LIGHT_READ: 'LIGHT_READ',
  MOISTURE_READ: 'MOISTURE_READ',
  FLOW_READ: 'FLOW_READ',
  STATUS_GENERAL: 'STATUS_GENERAL',
  STATUS_LED: 'STATUS_LED',
  STATUS_PUMP: 'STATUS_PUMP',
  PROFILE: 'PROFILE',
});

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

/**
 * Sends a command payload to ESP32 command endpoint.
 * Expected firmware payload shape:
 * { command: 'TEMP_READ', ...optionalArgs }
 */
export async function runCommand(command, payload = {}) {
  if (!command) {
    throw new Error('Command is required');
  }

  return fetchWithTimeout(COMMAND_ENDPOINT, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      command,
      ...payload,
    }),
  });
}

/**
 * Fetches sensor data from ESP32
 * @returns {Promise<Object>} - Sensor data as JSON
 */
export async function getSensors() {
  return fetchWithTimeout('/sensors', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

/**
 * Starts the pump on ESP32
 * @returns {Promise<Object>} - Response from ESP32
 */
export async function startPump() {
  return runPumpOn();
}

/**
 * Stops the pump on ESP32
 * @returns {Promise<Object>} - Response from ESP32
 */
export async function stopPump() {
  return runPumpOff();
}

/**
 * Sends plant profile to ESP32
 * @param {Object} profile - Plant profile object
 * @returns {Promise<Object>} - Response from ESP32
 */
export async function sendPlantProfile(profile) {
  return runProfile(profile);
}

// Command prototype wrappers
export async function runLedOn() {
  return runCommand(ESP32_COMMANDS.LED_ON);
}

export async function runLedOff() {
  return runCommand(ESP32_COMMANDS.LED_OFF);
}

export async function runPumpOn() {
  return runCommand(ESP32_COMMANDS.PUMP_ON);
}

export async function runPumpOff() {
  return runCommand(ESP32_COMMANDS.PUMP_OFF);
}

export async function runLedRead() {
  return runCommand(ESP32_COMMANDS.LED_READ);
}

export async function runTempRead() {
  return runCommand(ESP32_COMMANDS.TEMP_READ);
}

export async function runHumidityRead() {
  return runCommand(ESP32_COMMANDS.HUMIDITY_READ);
}

export async function runLightRead() {
  return runCommand(ESP32_COMMANDS.LIGHT_READ);
}

export async function runMoistureRead() {
  return runCommand(ESP32_COMMANDS.MOISTURE_READ);
}

export async function runFlowRead() {
  return runCommand(ESP32_COMMANDS.FLOW_READ);
}

export async function runStatusGeneral() {
  return runCommand(ESP32_COMMANDS.STATUS_GENERAL);
}

export async function runStatusLed() {
  return runCommand(ESP32_COMMANDS.STATUS_LED);
}

export async function runStatusPump() {
  return runCommand(ESP32_COMMANDS.STATUS_PUMP);
}

export async function runProfile(profile = {}) {
  return runCommand(ESP32_COMMANDS.PROFILE, { profile });
}

