(globalThis.TURBOPACK || (globalThis.TURBOPACK = [])).push([typeof document === "object" ? document.currentScript : undefined,
"[project]/lib/espApi.jsx [app-client] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "ESP32_COMMANDS",
    ()=>ESP32_COMMANDS,
    "getSensors",
    ()=>getSensors,
    "runCommand",
    ()=>runCommand,
    "runFlowRead",
    ()=>runFlowRead,
    "runHumidityRead",
    ()=>runHumidityRead,
    "runLedOff",
    ()=>runLedOff,
    "runLedOn",
    ()=>runLedOn,
    "runLedRead",
    ()=>runLedRead,
    "runLightRead",
    ()=>runLightRead,
    "runMoistureRead",
    ()=>runMoistureRead,
    "runProfile",
    ()=>runProfile,
    "runPumpOff",
    ()=>runPumpOff,
    "runPumpOn",
    ()=>runPumpOn,
    "runStatusGeneral",
    ()=>runStatusGeneral,
    "runStatusLed",
    ()=>runStatusLed,
    "runStatusPump",
    ()=>runStatusPump,
    "runTempRead",
    ()=>runTempRead,
    "sendPlantProfile",
    ()=>sendPlantProfile,
    "startPump",
    ()=>startPump,
    "stopPump",
    ()=>stopPump
]);
const BASE_URL = 'http://192.168.4.1';
const DEFAULT_TIMEOUT = 5000; // 5 seconds
const COMMAND_ENDPOINT = '/command';
const ESP32_COMMANDS = Object.freeze({
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
    PROFILE: 'PROFILE'
});
/**
 * Makes a fetch request with timeout and error handling
 * @param {string} endpoint - The API endpoint to call
 * @param {RequestInit} options - Fetch options (method, body, etc.)
 * @param {number} timeout - Request timeout in milliseconds
 * @returns {Promise<any>} - Parsed JSON response
 */ async function fetchWithTimeout(endpoint, options = {}, timeout = DEFAULT_TIMEOUT) {
    const controller = new AbortController();
    const timeoutId = setTimeout(()=>controller.abort(), timeout);
    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            ...options,
            signal: controller.signal
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
async function runCommand(command, payload = {}) {
    if (!command) {
        throw new Error('Command is required');
    }
    return fetchWithTimeout(COMMAND_ENDPOINT, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            command,
            ...payload
        })
    });
}
async function getSensors() {
    return fetchWithTimeout('/sensors', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });
}
async function startPump() {
    return runPumpOn();
}
async function stopPump() {
    return runPumpOff();
}
async function sendPlantProfile(profile) {
    return runProfile(profile);
}
async function runLedOn() {
    return runCommand(ESP32_COMMANDS.LED_ON);
}
async function runLedOff() {
    return runCommand(ESP32_COMMANDS.LED_OFF);
}
async function runPumpOn() {
    return runCommand(ESP32_COMMANDS.PUMP_ON);
}
async function runPumpOff() {
    return runCommand(ESP32_COMMANDS.PUMP_OFF);
}
async function runLedRead() {
    return runCommand(ESP32_COMMANDS.LED_READ);
}
async function runTempRead() {
    return runCommand(ESP32_COMMANDS.TEMP_READ);
}
async function runHumidityRead() {
    return runCommand(ESP32_COMMANDS.HUMIDITY_READ);
}
async function runLightRead() {
    return runCommand(ESP32_COMMANDS.LIGHT_READ);
}
async function runMoistureRead() {
    return runCommand(ESP32_COMMANDS.MOISTURE_READ);
}
async function runFlowRead() {
    return runCommand(ESP32_COMMANDS.FLOW_READ);
}
async function runStatusGeneral() {
    return runCommand(ESP32_COMMANDS.STATUS_GENERAL);
}
async function runStatusLed() {
    return runCommand(ESP32_COMMANDS.STATUS_LED);
}
async function runStatusPump() {
    return runCommand(ESP32_COMMANDS.STATUS_PUMP);
}
async function runProfile(profile = {}) {
    return runCommand(ESP32_COMMANDS.PROFILE, {
        profile
    });
}
if (typeof globalThis.$RefreshHelpers$ === 'object' && globalThis.$RefreshHelpers !== null) {
    __turbopack_context__.k.registerExports(__turbopack_context__.m, globalThis.$RefreshHelpers$);
}
}),
"[project]/lib/useEspConnection.jsx [app-client] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "useEspConnection",
    ()=>useEspConnection
]);
var __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/node_modules/next/dist/compiled/react/index.js [app-client] (ecmascript)");
var _s = __turbopack_context__.k.signature();
'use client';
;
const ESP32_BASE_URL = 'http://192.168.4.1';
const PING_INTERVAL = 3000; // Ping every 3 seconds
const PING_TIMEOUT = 2000; // 2 second timeout for ping
const PING_ENDPOINT = '/api/status'; // Adjust this endpoint based on your ESP32 API
function useEspConnection(interval = PING_INTERVAL) {
    _s();
    const [isConnected, setIsConnected] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useState"])(false);
    const [lastResponseTime, setLastResponseTime] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useState"])(null);
    const intervalRef = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useRef"])(null);
    const mountedRef = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useRef"])(true);
    const pingEsp32 = async ()=>{
        const startTime = Date.now();
        const controller = new AbortController();
        const timeoutId = setTimeout(()=>controller.abort(), PING_TIMEOUT);
        try {
            const response = await fetch(`${ESP32_BASE_URL}${PING_ENDPOINT}`, {
                method: 'GET',
                signal: controller.signal,
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            clearTimeout(timeoutId);
            const responseTime = Date.now() - startTime;
            if (response.ok && mountedRef.current) {
                setIsConnected(true);
                setLastResponseTime(responseTime);
            } else if (mountedRef.current) {
                setIsConnected(false);
                setLastResponseTime(null);
            }
        } catch (error) {
            clearTimeout(timeoutId);
            if (mountedRef.current) {
                setIsConnected(false);
                setLastResponseTime(null);
            }
        }
    };
    (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useEffect"])({
        "useEspConnection.useEffect": ()=>{
            mountedRef.current = true;
            // Initial ping
            pingEsp32();
            // Set up interval for periodic pings
            intervalRef.current = setInterval({
                "useEspConnection.useEffect": ()=>{
                    pingEsp32();
                }
            }["useEspConnection.useEffect"], interval);
            // Cleanup function
            return ({
                "useEspConnection.useEffect": ()=>{
                    mountedRef.current = false;
                    if (intervalRef.current) {
                        clearInterval(intervalRef.current);
                    }
                }
            })["useEspConnection.useEffect"];
        }
    }["useEspConnection.useEffect"], [
        interval
    ]);
    return {
        isConnected,
        lastResponseTime
    };
}
_s(useEspConnection, "XaDgQ9U4qyIjCMMbFT1gLmw5oEw=");
if (typeof globalThis.$RefreshHelpers$ === 'object' && globalThis.$RefreshHelpers !== null) {
    __turbopack_context__.k.registerExports(__turbopack_context__.m, globalThis.$RefreshHelpers$);
}
}),
"[project]/app/dashboard/page.jsx [app-client] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "default",
    ()=>Dashboard
]);
var __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/node_modules/next/dist/compiled/react/jsx-dev-runtime.js [app-client] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/node_modules/next/dist/compiled/react/index.js [app-client] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$espApi$2e$jsx__$5b$app$2d$client$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/lib/espApi.jsx [app-client] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$useEspConnection$2e$jsx__$5b$app$2d$client$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/lib/useEspConnection.jsx [app-client] (ecmascript)");
;
var _s = __turbopack_context__.k.signature();
'use client';
;
;
;
function Dashboard() {
    _s();
    const { isConnected, lastResponseTime } = (0, __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$useEspConnection$2e$jsx__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useEspConnection"])();
    const [sensorData, setSensorData] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useState"])({
        soilMoisture: null,
        temperature: null,
        humidity: null,
        lightIntensity: null,
        waterLevel: null,
        batteryPercentage: null
    });
    const [loading, setLoading] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useState"])(true);
    const [error, setError] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useState"])(null);
    const intervalRef = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useRef"])(null);
    const fetchSensorData = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useCallback"])({
        "Dashboard.useCallback[fetchSensorData]": async ()=>{
            if (!isConnected) {
                setError('ESP32 is not connected');
                setLoading(false);
                return;
            }
            try {
                setError(null);
                const data = await (0, __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$espApi$2e$jsx__$5b$app$2d$client$5d$__$28$ecmascript$29$__["getSensors"])();
                // Map the response data to our state
                // Adjust these keys based on your ESP32 API response structure
                setSensorData({
                    soilMoisture: data.soilMoisture ?? data.soil_moisture ?? data.moisture ?? null,
                    temperature: data.temperature ?? data.temp ?? null,
                    humidity: data.humidity ?? data.hum ?? null,
                    lightIntensity: data.lightIntensity ?? data.light ?? data.light_intensity ?? null,
                    waterLevel: data.waterLevel ?? data.water_level ?? data.level ?? null,
                    batteryPercentage: data.batteryPercentage ?? data.battery ?? data.battery_percentage ?? null
                });
                setLoading(false);
            } catch (err) {
                setError(err.message || 'Failed to fetch sensor data');
                setLoading(false);
            }
        }
    }["Dashboard.useCallback[fetchSensorData]"], [
        isConnected
    ]);
    (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$index$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useEffect"])({
        "Dashboard.useEffect": ()=>{
            // Initial fetch
            fetchSensorData();
            // Set up interval to fetch every 3 seconds
            intervalRef.current = setInterval({
                "Dashboard.useEffect": ()=>{
                    fetchSensorData();
                }
            }["Dashboard.useEffect"], 3000);
            return ({
                "Dashboard.useEffect": ()=>{
                    if (intervalRef.current) {
                        clearInterval(intervalRef.current);
                    }
                }
            })["Dashboard.useEffect"];
        }
    }["Dashboard.useEffect"], [
        fetchSensorData
    ]);
    const SensorCard = ({ title, value, unit, icon })=>{
        const displayValue = value !== null ? `${value}${unit ? ` ${unit}` : ''}` : 'N/A';
        return /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
            className: "bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-shadow",
            children: [
                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "flex items-center justify-between mb-4",
                    children: [
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("h3", {
                            className: "text-gray-600 dark:text-gray-400 text-sm font-medium uppercase tracking-wide",
                            children: title
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 71,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                            className: "text-2xl",
                            children: icon
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 74,
                            columnNumber: 11
                        }, this)
                    ]
                }, void 0, true, {
                    fileName: "[project]/app/dashboard/page.jsx",
                    lineNumber: 70,
                    columnNumber: 9
                }, this),
                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "text-3xl font-bold text-green-600 dark:text-green-400",
                    children: displayValue
                }, void 0, false, {
                    fileName: "[project]/app/dashboard/page.jsx",
                    lineNumber: 76,
                    columnNumber: 9
                }, this)
            ]
        }, void 0, true, {
            fileName: "[project]/app/dashboard/page.jsx",
            lineNumber: 69,
            columnNumber: 7
        }, this);
    };
    return /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
        className: "min-h-screen bg-gray-50 dark:bg-gray-900 p-6",
        children: /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
            className: "max-w-7xl mx-auto",
            children: [
                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "mb-8",
                    children: [
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("h1", {
                            className: "text-3xl font-bold text-gray-900 dark:text-white mb-2",
                            children: "Dashboard"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 88,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                            className: "flex items-center gap-4",
                            children: [
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    className: "flex items-center gap-2",
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                            className: `w-3 h-3 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'} animate-pulse`
                                        }, void 0, false, {
                                            fileName: "[project]/app/dashboard/page.jsx",
                                            lineNumber: 91,
                                            columnNumber: 15
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                            className: "text-sm text-gray-600 dark:text-gray-400",
                                            children: isConnected ? 'Connected' : 'Disconnected'
                                        }, void 0, false, {
                                            fileName: "[project]/app/dashboard/page.jsx",
                                            lineNumber: 96,
                                            columnNumber: 15
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/dashboard/page.jsx",
                                    lineNumber: 90,
                                    columnNumber: 13
                                }, this),
                                isConnected && lastResponseTime && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                    className: "text-sm text-gray-500 dark:text-gray-400",
                                    children: [
                                        "Response time: ",
                                        lastResponseTime,
                                        "ms"
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/dashboard/page.jsx",
                                    lineNumber: 101,
                                    columnNumber: 15
                                }, this)
                            ]
                        }, void 0, true, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 89,
                            columnNumber: 11
                        }, this)
                    ]
                }, void 0, true, {
                    fileName: "[project]/app/dashboard/page.jsx",
                    lineNumber: 87,
                    columnNumber: 9
                }, this),
                error && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "mb-6 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg",
                    children: error
                }, void 0, false, {
                    fileName: "[project]/app/dashboard/page.jsx",
                    lineNumber: 110,
                    columnNumber: 11
                }, this),
                loading && !error && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "mb-6 text-center py-8",
                    children: [
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                            className: "inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600 dark:border-green-400"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 118,
                            columnNumber: 13
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("p", {
                            className: "mt-2 text-gray-600 dark:text-gray-400",
                            children: "Loading sensor data..."
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 119,
                            columnNumber: 13
                        }, this)
                    ]
                }, void 0, true, {
                    fileName: "[project]/app/dashboard/page.jsx",
                    lineNumber: 117,
                    columnNumber: 11
                }, this),
                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6",
                    children: [
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])(SensorCard, {
                            title: "Soil Moisture",
                            value: sensorData.soilMoisture,
                            unit: "%",
                            icon: "🌱"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 125,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])(SensorCard, {
                            title: "Temperature",
                            value: sensorData.temperature,
                            unit: "°C",
                            icon: "🌡️"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 131,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])(SensorCard, {
                            title: "Humidity",
                            value: sensorData.humidity,
                            unit: "%",
                            icon: "💧"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 137,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])(SensorCard, {
                            title: "Light Intensity",
                            value: sensorData.lightIntensity,
                            unit: "lux",
                            icon: "☀️"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 143,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])(SensorCard, {
                            title: "Water Level",
                            value: sensorData.waterLevel,
                            unit: "%",
                            icon: "💦"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 149,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$compiled$2f$react$2f$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$client$5d$__$28$ecmascript$29$__["jsxDEV"])(SensorCard, {
                            title: "Battery",
                            value: sensorData.batteryPercentage,
                            unit: "%",
                            icon: "🔋"
                        }, void 0, false, {
                            fileName: "[project]/app/dashboard/page.jsx",
                            lineNumber: 155,
                            columnNumber: 11
                        }, this)
                    ]
                }, void 0, true, {
                    fileName: "[project]/app/dashboard/page.jsx",
                    lineNumber: 124,
                    columnNumber: 9
                }, this)
            ]
        }, void 0, true, {
            fileName: "[project]/app/dashboard/page.jsx",
            lineNumber: 85,
            columnNumber: 7
        }, this)
    }, void 0, false, {
        fileName: "[project]/app/dashboard/page.jsx",
        lineNumber: 84,
        columnNumber: 5
    }, this);
}
_s(Dashboard, "2qG4hik0eFj4dFOUPpck9p6YwXM=", false, function() {
    return [
        __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$useEspConnection$2e$jsx__$5b$app$2d$client$5d$__$28$ecmascript$29$__["useEspConnection"]
    ];
});
_c = Dashboard;
var _c;
__turbopack_context__.k.register(_c, "Dashboard");
if (typeof globalThis.$RefreshHelpers$ === 'object' && globalThis.$RefreshHelpers !== null) {
    __turbopack_context__.k.registerExports(__turbopack_context__.m, globalThis.$RefreshHelpers$);
}
}),
]);

//# sourceMappingURL=_fd70976c._.js.map