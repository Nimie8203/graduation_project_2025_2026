module.exports = [
"[project]/lib/espApi.jsx [app-ssr] (ecmascript)", ((__turbopack_context__) => {
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
}),
"[project]/lib/useEspConnection.jsx [app-ssr] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "useEspConnection",
    ()=>useEspConnection
]);
var __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react.js [app-ssr] (ecmascript)");
'use client';
;
const ESP32_BASE_URL = 'http://192.168.4.1';
const PING_INTERVAL = 3000; // Ping every 3 seconds
const PING_TIMEOUT = 2000; // 2 second timeout for ping
const PING_ENDPOINT = '/api/status'; // Adjust this endpoint based on your ESP32 API
function useEspConnection(interval = PING_INTERVAL) {
    const [isConnected, setIsConnected] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(false);
    const [lastResponseTime, setLastResponseTime] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(null);
    const intervalRef = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useRef"])(null);
    const mountedRef = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useRef"])(true);
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
    (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useEffect"])(()=>{
        mountedRef.current = true;
        // Initial ping
        pingEsp32();
        // Set up interval for periodic pings
        intervalRef.current = setInterval(()=>{
            pingEsp32();
        }, interval);
        // Cleanup function
        return ()=>{
            mountedRef.current = false;
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
            }
        };
    }, [
        interval
    ]);
    return {
        isConnected,
        lastResponseTime
    };
}
}),
"[project]/lib/mockPlantData.jsx [app-ssr] (ecmascript)", ((__turbopack_context__) => {
"use strict";

// Mock plant data for demo purposes
// This simulates plant data that would come from ESP32
__turbopack_context__.s([
    "getMockPlantData",
    ()=>getMockPlantData,
    "mockPlantData",
    ()=>mockPlantData
]);
const mockPlantData = {
    plants: [
        {
            id: '1',
            name: 'Tomato Plant',
            timesPerDay: 2,
            timeOfDay: [
                '08:00',
                '18:00'
            ],
            waterAmount: 500
        },
        {
            id: '2',
            name: 'Basil',
            timesPerDay: 1,
            timeOfDay: [
                '09:00'
            ],
            waterAmount: 300
        },
        {
            id: '3',
            name: 'Lettuce',
            timesPerDay: 3,
            timeOfDay: [
                '07:00',
                '13:00',
                '19:00'
            ],
            waterAmount: 400
        }
    ]
};
async function getMockPlantData() {
    // Simulate API delay
    return new Promise((resolve)=>{
        setTimeout(()=>{
            resolve(mockPlantData);
        }, 500);
    });
}
}),
"[project]/app/plants/page.jsx [app-ssr] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "default",
    ()=>Plants
]);
var __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react-jsx-dev-runtime.js [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react.js [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$espApi$2e$jsx__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/lib/espApi.jsx [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$useEspConnection$2e$jsx__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/lib/useEspConnection.jsx [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$mockPlantData$2e$jsx__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/lib/mockPlantData.jsx [app-ssr] (ecmascript)");
'use client';
;
;
;
;
;
function Plants() {
    const { isConnected } = (0, __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$useEspConnection$2e$jsx__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useEspConnection"])();
    const [profiles, setProfiles] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])([]);
    const [activeProfileId, setActiveProfileId] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(null);
    const [loading, setLoading] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(false);
    const [message, setMessage] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])({
        type: null,
        text: ''
    });
    const [showForm, setShowForm] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(false);
    const [editingProfile, setEditingProfile] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(null);
    const [formData, setFormData] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])({
        name: '',
        timesPerDay: 1,
        timeOfDay: [
            '08:00'
        ],
        waterAmount: 500
    });
    // Load profiles from localStorage on mount
    (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useEffect"])(()=>{
        const loadData = async ()=>{
            // First load from localStorage
            const saved = localStorage.getItem('plantProfiles');
            if (saved) {
                try {
                    const parsed = JSON.parse(saved);
                    setProfiles(parsed);
                    // Load active profile
                    const activeId = localStorage.getItem('activePlantProfile');
                    if (activeId) {
                        setActiveProfileId(activeId);
                    }
                } catch (error) {
                    console.error('Error loading profiles:', error);
                }
            } else {
                // If no saved profiles, load mock data for demo
                try {
                    const mockData = await (0, __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$mockPlantData$2e$jsx__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["getMockPlantData"])();
                    if (mockData.plants) {
                        setProfiles(mockData.plants);
                    }
                } catch (error) {
                    console.error('Error loading mock data:', error);
                }
            }
        };
        loadData();
    }, []);
    const saveProfiles = (newProfiles)=>{
        try {
            localStorage.setItem('plantProfiles', JSON.stringify(newProfiles));
            setProfiles(newProfiles);
        } catch (error) {
            console.error('Error saving profiles:', error);
            setMessage({
                type: 'error',
                text: 'Failed to save profile'
            });
        }
    };
    const handleSubmit = (e)=>{
        e.preventDefault();
        if (!formData.name.trim()) {
            setMessage({
                type: 'error',
                text: 'Plant name is required'
            });
            return;
        }
        if (formData.timesPerDay < 1 || formData.timesPerDay > 10) {
            setMessage({
                type: 'error',
                text: 'Times per day must be between 1 and 10'
            });
            return;
        }
        if (formData.waterAmount < 1) {
            setMessage({
                type: 'error',
                text: 'Water amount must be greater than 0'
            });
            return;
        }
        const newProfiles = [
            ...profiles
        ];
        if (editingProfile) {
            // Update existing profile
            const index = newProfiles.findIndex((p)=>p.id === editingProfile.id);
            if (index !== -1) {
                newProfiles[index] = {
                    ...editingProfile,
                    ...formData
                };
            }
        } else {
            // Create new profile
            const newProfile = {
                id: Date.now().toString(),
                ...formData
            };
            newProfiles.push(newProfile);
        }
        saveProfiles(newProfiles);
        resetForm();
        setMessage({
            type: 'success',
            text: editingProfile ? 'Profile updated!' : 'Profile created!'
        });
    };
    const resetForm = ()=>{
        setFormData({
            name: '',
            timesPerDay: 1,
            timeOfDay: [
                '08:00'
            ],
            waterAmount: 500
        });
        setEditingProfile(null);
        setShowForm(false);
    };
    const handleEdit = (profile)=>{
        setEditingProfile(profile);
        setFormData({
            name: profile.name,
            timesPerDay: profile.timesPerDay,
            timeOfDay: [
                ...profile.timeOfDay
            ],
            waterAmount: profile.waterAmount
        });
        setShowForm(true);
    };
    const handleDelete = (id)=>{
        if (confirm('Are you sure you want to delete this profile?')) {
            const newProfiles = profiles.filter((p)=>p.id !== id);
            saveProfiles(newProfiles);
            if (activeProfileId === id) {
                setActiveProfileId(null);
                localStorage.removeItem('activePlantProfile');
            }
            setMessage({
                type: 'success',
                text: 'Profile deleted!'
            });
        }
    };
    const handleSetActive = async (profile)=>{
        setActiveProfileId(profile.id);
        localStorage.setItem('activePlantProfile', profile.id);
        if (isConnected) {
            setLoading(true);
            setMessage({
                type: null,
                text: ''
            });
            try {
                await (0, __TURBOPACK__imported__module__$5b$project$5d2f$lib$2f$espApi$2e$jsx__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["sendPlantProfile"])(profile);
                setMessage({
                    type: 'success',
                    text: 'Active profile sent to ESP32!'
                });
            } catch (error) {
                setMessage({
                    type: 'error',
                    text: error.message || 'Failed to send profile to ESP32'
                });
            } finally{
                setLoading(false);
            }
        } else {
            setMessage({
                type: 'warning',
                text: 'Profile set as active, but ESP32 is not connected. Will send when connected.'
            });
        }
    };
    const handleTimeChange = (index, value)=>{
        const newTimes = [
            ...formData.timeOfDay
        ];
        newTimes[index] = value;
        setFormData({
            ...formData,
            timeOfDay: newTimes
        });
    };
    const handleTimesPerDayChange = (value)=>{
        const times = parseInt(value) || 1;
        setFormData({
            ...formData,
            timesPerDay: times,
            timeOfDay: Array(times).fill('').map((_, i)=>formData.timeOfDay[i] || '08:00')
        });
    };
    const activeProfile = profiles.find((p)=>p.id === activeProfileId);
    return /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
        className: "min-h-screen bg-gray-50 dark:bg-gray-900 p-6",
        children: /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
            className: "max-w-6xl mx-auto",
            children: [
                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "mb-8",
                    children: [
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                            className: "flex items-center justify-between mb-4",
                            children: [
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("h1", {
                                    className: "text-3xl font-bold text-gray-900 dark:text-white",
                                    children: "Plant Profiles"
                                }, void 0, false, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 187,
                                    columnNumber: 13
                                }, this),
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("button", {
                                    onClick: ()=>{
                                        resetForm();
                                        setShowForm(true);
                                    },
                                    className: "px-6 py-3 bg-green-600 dark:bg-green-500 text-white rounded-lg font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors shadow-md hover:shadow-lg",
                                    children: "+ New Profile"
                                }, void 0, false, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 188,
                                    columnNumber: 13
                                }, this)
                            ]
                        }, void 0, true, {
                            fileName: "[project]/app/plants/page.jsx",
                            lineNumber: 186,
                            columnNumber: 11
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                            className: "flex items-center gap-4",
                            children: [
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    className: "flex items-center gap-2",
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                            className: `w-3 h-3 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'} animate-pulse`
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 200,
                                            columnNumber: 15
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                            className: "text-sm text-gray-600 dark:text-gray-400",
                                            children: isConnected ? 'Connected' : 'Disconnected'
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 205,
                                            columnNumber: 15
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 199,
                                    columnNumber: 13
                                }, this),
                                activeProfile && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                    className: "text-sm text-green-600 dark:text-green-400 font-medium",
                                    children: [
                                        "Active: ",
                                        activeProfile.name
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 210,
                                    columnNumber: 15
                                }, this)
                            ]
                        }, void 0, true, {
                            fileName: "[project]/app/plants/page.jsx",
                            lineNumber: 198,
                            columnNumber: 11
                        }, this)
                    ]
                }, void 0, true, {
                    fileName: "[project]/app/plants/page.jsx",
                    lineNumber: 185,
                    columnNumber: 9
                }, this),
                message.type && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: `mb-6 px-4 py-3 rounded-lg ${message.type === 'success' ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-800 dark:text-green-400' : message.type === 'warning' ? 'bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 text-yellow-800 dark:text-yellow-400' : 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-800 dark:text-red-400'}`,
                    children: message.text
                }, void 0, false, {
                    fileName: "[project]/app/plants/page.jsx",
                    lineNumber: 219,
                    columnNumber: 11
                }, this),
                showForm && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "mb-8 bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border border-gray-200 dark:border-gray-700",
                    children: [
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("h2", {
                            className: "text-xl font-semibold text-gray-900 dark:text-white mb-6",
                            children: editingProfile ? 'Edit Profile' : 'New Plant Profile'
                        }, void 0, false, {
                            fileName: "[project]/app/plants/page.jsx",
                            lineNumber: 235,
                            columnNumber: 13
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("form", {
                            onSubmit: handleSubmit,
                            className: "space-y-6",
                            children: [
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("label", {
                                            className: "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2",
                                            children: "Plant Name"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 240,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("input", {
                                            type: "text",
                                            value: formData.name,
                                            onChange: (e)=>setFormData({
                                                    ...formData,
                                                    name: e.target.value
                                                }),
                                            className: "w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent",
                                            placeholder: "e.g., Tomato Plant",
                                            required: true
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 243,
                                            columnNumber: 17
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 239,
                                    columnNumber: 15
                                }, this),
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("label", {
                                            className: "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2",
                                            children: "Times Per Day"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 254,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("input", {
                                            type: "number",
                                            min: "1",
                                            max: "10",
                                            value: formData.timesPerDay,
                                            onChange: (e)=>handleTimesPerDayChange(e.target.value),
                                            className: "w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent",
                                            required: true
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 257,
                                            columnNumber: 17
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 253,
                                    columnNumber: 15
                                }, this),
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("label", {
                                            className: "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2",
                                            children: "Time of Day"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 269,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                            className: "space-y-2",
                                            children: formData.timeOfDay.map((time, index)=>/*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("input", {
                                                    type: "time",
                                                    value: time,
                                                    onChange: (e)=>handleTimeChange(index, e.target.value),
                                                    className: "w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent",
                                                    required: true
                                                }, index, false, {
                                                    fileName: "[project]/app/plants/page.jsx",
                                                    lineNumber: 274,
                                                    columnNumber: 21
                                                }, this))
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 272,
                                            columnNumber: 17
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 268,
                                    columnNumber: 15
                                }, this),
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("label", {
                                            className: "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2",
                                            children: "Water Amount (ml)"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 287,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("input", {
                                            type: "number",
                                            min: "1",
                                            value: formData.waterAmount,
                                            onChange: (e)=>setFormData({
                                                    ...formData,
                                                    waterAmount: parseInt(e.target.value) || 0
                                                }),
                                            className: "w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent",
                                            required: true
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 290,
                                            columnNumber: 17
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 286,
                                    columnNumber: 15
                                }, this),
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    className: "flex gap-4",
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("button", {
                                            type: "submit",
                                            className: "flex-1 px-6 py-3 bg-green-600 dark:bg-green-500 text-white rounded-lg font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors shadow-md hover:shadow-lg",
                                            children: editingProfile ? 'Update Profile' : 'Create Profile'
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 301,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("button", {
                                            type: "button",
                                            onClick: resetForm,
                                            className: "flex-1 px-6 py-3 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors",
                                            children: "Cancel"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 307,
                                            columnNumber: 17
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 300,
                                    columnNumber: 15
                                }, this)
                            ]
                        }, void 0, true, {
                            fileName: "[project]/app/plants/page.jsx",
                            lineNumber: 238,
                            columnNumber: 13
                        }, this)
                    ]
                }, void 0, true, {
                    fileName: "[project]/app/plants/page.jsx",
                    lineNumber: 234,
                    columnNumber: 11
                }, this),
                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6",
                    children: profiles.map((profile)=>/*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                            className: `bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border-2 transition-all ${activeProfileId === profile.id ? 'border-green-500 dark:border-green-400 shadow-lg' : 'border-gray-200 dark:border-gray-700'}`,
                            children: [
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    className: "flex items-start justify-between mb-4",
                                    children: /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                        children: [
                                            /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("h3", {
                                                className: "text-xl font-bold text-gray-900 dark:text-white mb-1",
                                                children: profile.name
                                            }, void 0, false, {
                                                fileName: "[project]/app/plants/page.jsx",
                                                lineNumber: 332,
                                                columnNumber: 19
                                            }, this),
                                            activeProfileId === profile.id && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                                className: "inline-block px-2 py-1 text-xs font-semibold bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400 rounded",
                                                children: "Active"
                                            }, void 0, false, {
                                                fileName: "[project]/app/plants/page.jsx",
                                                lineNumber: 336,
                                                columnNumber: 21
                                            }, this)
                                        ]
                                    }, void 0, true, {
                                        fileName: "[project]/app/plants/page.jsx",
                                        lineNumber: 331,
                                        columnNumber: 17
                                    }, this)
                                }, void 0, false, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 330,
                                    columnNumber: 15
                                }, this),
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    className: "space-y-3 mb-4",
                                    children: [
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                            className: "flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400",
                                            children: [
                                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                                    children: "🕐"
                                                }, void 0, false, {
                                                    fileName: "[project]/app/plants/page.jsx",
                                                    lineNumber: 345,
                                                    columnNumber: 19
                                                }, this),
                                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                                    children: [
                                                        profile.timesPerDay,
                                                        " time",
                                                        profile.timesPerDay > 1 ? 's' : '',
                                                        " per day"
                                                    ]
                                                }, void 0, true, {
                                                    fileName: "[project]/app/plants/page.jsx",
                                                    lineNumber: 346,
                                                    columnNumber: 19
                                                }, this)
                                            ]
                                        }, void 0, true, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 344,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                            className: "text-sm text-gray-600 dark:text-gray-400",
                                            children: [
                                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                                    className: "font-medium mb-1",
                                                    children: "Times:"
                                                }, void 0, false, {
                                                    fileName: "[project]/app/plants/page.jsx",
                                                    lineNumber: 349,
                                                    columnNumber: 19
                                                }, this),
                                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                                    className: "flex flex-wrap gap-2",
                                                    children: profile.timeOfDay.map((time, idx)=>/*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                                            className: "px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded text-xs",
                                                            children: time
                                                        }, idx, false, {
                                                            fileName: "[project]/app/plants/page.jsx",
                                                            lineNumber: 352,
                                                            columnNumber: 23
                                                        }, this))
                                                }, void 0, false, {
                                                    fileName: "[project]/app/plants/page.jsx",
                                                    lineNumber: 350,
                                                    columnNumber: 19
                                                }, this)
                                            ]
                                        }, void 0, true, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 348,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                            className: "flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400",
                                            children: [
                                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                                    children: "💧"
                                                }, void 0, false, {
                                                    fileName: "[project]/app/plants/page.jsx",
                                                    lineNumber: 362,
                                                    columnNumber: 19
                                                }, this),
                                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("span", {
                                                    children: [
                                                        profile.waterAmount,
                                                        " ml per watering"
                                                    ]
                                                }, void 0, true, {
                                                    fileName: "[project]/app/plants/page.jsx",
                                                    lineNumber: 363,
                                                    columnNumber: 19
                                                }, this)
                                            ]
                                        }, void 0, true, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 361,
                                            columnNumber: 17
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 343,
                                    columnNumber: 15
                                }, this),
                                /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                                    className: "flex gap-2",
                                    children: [
                                        activeProfileId !== profile.id && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("button", {
                                            onClick: ()=>handleSetActive(profile),
                                            disabled: loading,
                                            className: "flex-1 px-4 py-2 bg-green-600 dark:bg-green-500 text-white rounded-lg text-sm font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed",
                                            children: "Set Active"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 369,
                                            columnNumber: 19
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("button", {
                                            onClick: ()=>handleEdit(profile),
                                            className: "px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg text-sm font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors",
                                            children: "Edit"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 377,
                                            columnNumber: 17
                                        }, this),
                                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("button", {
                                            onClick: ()=>handleDelete(profile.id),
                                            className: "px-4 py-2 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded-lg text-sm font-semibold hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors",
                                            children: "Delete"
                                        }, void 0, false, {
                                            fileName: "[project]/app/plants/page.jsx",
                                            lineNumber: 383,
                                            columnNumber: 17
                                        }, this)
                                    ]
                                }, void 0, true, {
                                    fileName: "[project]/app/plants/page.jsx",
                                    lineNumber: 367,
                                    columnNumber: 15
                                }, this)
                            ]
                        }, profile.id, true, {
                            fileName: "[project]/app/plants/page.jsx",
                            lineNumber: 322,
                            columnNumber: 13
                        }, this))
                }, void 0, false, {
                    fileName: "[project]/app/plants/page.jsx",
                    lineNumber: 320,
                    columnNumber: 9
                }, this),
                profiles.length === 0 && !showForm && /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("div", {
                    className: "text-center py-12 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700",
                    children: [
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("p", {
                            className: "text-gray-600 dark:text-gray-400 mb-4",
                            children: "No plant profiles yet"
                        }, void 0, false, {
                            fileName: "[project]/app/plants/page.jsx",
                            lineNumber: 396,
                            columnNumber: 13
                        }, this),
                        /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])("button", {
                            onClick: ()=>setShowForm(true),
                            className: "px-6 py-3 bg-green-600 dark:bg-green-500 text-white rounded-lg font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors",
                            children: "Create Your First Profile"
                        }, void 0, false, {
                            fileName: "[project]/app/plants/page.jsx",
                            lineNumber: 397,
                            columnNumber: 13
                        }, this)
                    ]
                }, void 0, true, {
                    fileName: "[project]/app/plants/page.jsx",
                    lineNumber: 395,
                    columnNumber: 11
                }, this)
            ]
        }, void 0, true, {
            fileName: "[project]/app/plants/page.jsx",
            lineNumber: 183,
            columnNumber: 7
        }, this)
    }, void 0, false, {
        fileName: "[project]/app/plants/page.jsx",
        lineNumber: 182,
        columnNumber: 5
    }, this);
}
}),
];

//# sourceMappingURL=_eb057f7d._.js.map