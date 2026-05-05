'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { getSensors } from '@/lib/espApi';
import { useEspConnection } from '@/lib/useEspConnection';

export default function Dashboard() {
  const { isConnected, lastResponseTime } = useEspConnection();
  const [sensorData, setSensorData] = useState({
    soilMoisture1: null,
    soilMoisture2: null,
    soilMoisture3: null,
    soilMoisture4: null,
    temperature: null,
    humidity: null,
    lightIntensity: null,
    waterLevel: null,
    flowSensor1: null,
    flowSensor2: null,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const intervalRef = useRef(null);

  const moistureSensors = [
    sensorData.soilMoisture1,
    sensorData.soilMoisture2,
    sensorData.soilMoisture3,
    sensorData.soilMoisture4,
  ];

  const hasWaterFlow =
    Number(sensorData.flowSensor1 ?? 0) > 0 ||
    Number(sensorData.flowSensor2 ?? 0) > 0;

  const avgMoisture = moistureSensors.filter((v) => v !== null).length
    ? (
        moistureSensors
          .filter((v) => v !== null)
          .reduce((sum, v) => sum + Number(v), 0) /
        moistureSensors.filter((v) => v !== null).length
      ).toFixed(1)
    : null;

  const fetchSensorData = useCallback(async () => {
    if (!isConnected) {
      setError('ESP32 is not connected');
      setLoading(false);
      return;
    }
    try {
      setError(null);
      const data = await getSensors();
      setSensorData({
        soilMoisture1: data.soilMoisture1 ?? data.soil_moisture_1 ?? data.moisture_1 ?? null,
        soilMoisture2: data.soilMoisture2 ?? data.soil_moisture_2 ?? data.moisture_2 ?? null,
        soilMoisture3: data.soilMoisture3 ?? data.soil_moisture_3 ?? data.moisture_3 ?? null,
        soilMoisture4: data.soilMoisture4 ?? data.soil_moisture_4 ?? data.moisture_4 ?? null,
        temperature: data.temperature ?? data.temp ?? null,
        humidity: data.humidity ?? data.hum ?? null,
        lightIntensity: data.lightIntensity ?? data.light ?? data.light_intensity ?? null,
        waterLevel: data.waterLevel ?? data.water_level ?? data.level ?? null,
        flowSensor1: data.flowSensor1 ?? data.flow_sensor_1 ?? null,
        flowSensor2: data.flowSensor2 ?? data.flow_sensor_2 ?? null,
      });
      setLoading(false);
    } catch (err) {
      setError(err.message || 'Failed to fetch sensor data');
      setLoading(false);
    }
  }, [isConnected]);

  useEffect(() => {
    fetchSensorData();
    intervalRef.current = setInterval(fetchSensorData, 3000);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [fetchSensorData]);

  // ─── Sub-components ────────────────────────────────────────────────────────

  const SensorCard = ({ title, value, unit, icon }) => {
    const displayValue = value !== null ? `${value}${unit ? ` ${unit}` : ''}` : 'N/A';
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-shadow">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-gray-600 dark:text-gray-400 text-sm font-medium uppercase tracking-wide">
            {title}
          </h3>
          <span className="text-2xl">{icon}</span>
        </div>
        <div className="text-3xl font-bold text-green-600 dark:text-green-400">
          {displayValue}
        </div>
      </div>
    );
  };

  // ─── Irrigation Flow Visual (illustration-style) ────────────────────────────

  const IrrigationVisual = () => {
    const waterPct = Math.max(0, Math.min(100, Number(sensorData.waterLevel ?? 0)));
    const tankFillY = 55 + 140 * (1 - waterPct / 100);
    const tankFillH = 140 * (waterPct / 100);

    const moistureBar = (value) => {
      const pct = value !== null ? Math.max(0, Math.min(100, Number(value))) : 0;
      return { pct, label: value !== null ? `${pct}%` : 'N/A' };
    };

    const m1 = moistureBar(sensorData.soilMoisture1);
    const m2 = moistureBar(sensorData.soilMoisture2);
    const m3 = moistureBar(sensorData.soilMoisture3);
    const m4 = moistureBar(sensorData.soilMoisture4);

    return (
      <div className="mb-8 bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 p-5">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            Irrigation Flow Visual
          </h2>
          <div className="text-sm text-gray-600 dark:text-gray-400">
            Avg Moisture:{' '}
            <span className="font-semibold text-green-600 dark:text-green-400">
              {avgMoisture !== null ? `${avgMoisture}%` : 'N/A'}
            </span>
          </div>
        </div>

        {/* SVG Illustration */}
        <svg
          viewBox="0 0 680 260"
          xmlns="http://www.w3.org/2000/svg"
          className="w-full h-auto block"
        >
          <defs>
            <marker id="arr" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
              <path d="M0,0 L6,3 L0,6 Z" fill="#60a5fa" />
            </marker>
            <clipPath id="tankClip">
              <rect x="20" y="55" width="115" height="140" rx="6" />
            </clipPath>
          </defs>

          

          {/* ── Water Tank ── */}
          <rect x="20" y="55" width="115" height="140" rx="6" fill="#1e293b" stroke="#334155" strokeWidth="1.5" />
          {/* Vent cap */}
          <rect x="68" y="48" width="20" height="10" rx="3" fill="#475569" />
          {/* Water fill (dynamic) */}
          <rect
            x="20"
            y={tankFillY}
            width="115"
            height={tankFillH}
            fill="#38bdf8"
            opacity="0.35"
            clipPath="url(#tankClip)"
          />
          {/* Level text */}
          <text x="77" y="150" textAnchor="middle" fontSize="13" fontWeight="500" fill="#7dd3fc">
            {sensorData.waterLevel !== null ? `${sensorData.waterLevel}%` : 'N/A'}
          </text>
          <text x="77" y="166" textAnchor="middle" fontSize="10" fill="#94a3b8">
            water level
          </text>

          {/* ── Pump 1 ── */}
          <rect x="140" y="88" width="30" height="22" rx="4" fill="#334155" stroke="#475569" strokeWidth="1" />
          <circle cx="155" cy="99" r="7" fill="none" stroke="#60a5fa" strokeWidth="1.5" />
          <circle cx="155" cy="99" r="3" fill="#60a5fa" />
          <text x="155" y="122" textAnchor="middle" fontSize="9" fill="#94a3b8">pump 1</text>

          {/* ── Pump 2 ── */}
          <rect x="140" y="138" width="30" height="22" rx="4" fill="#334155" stroke="#475569" strokeWidth="1" />
          <circle cx="155" cy="149" r="7" fill="none" stroke="#60a5fa" strokeWidth="1.5" />
          <circle cx="155" cy="149" r="3" fill="#60a5fa" />
          <text x="155" y="172" textAnchor="middle" fontSize="9" fill="#94a3b8">pump 2</text>

          {/* ── Pipes from tank → junction → garden ── */}
          <path d="M 170 99 Q 210 99 210 119" stroke="#60a5fa" strokeWidth="3" fill="none" strokeLinecap="round" />
          <path d="M 170 149 Q 210 149 210 134" stroke="#60a5fa" strokeWidth="3" fill="none" strokeLinecap="round" />
          <line x1="210" y1="99" x2="210" y2="160" stroke="#60a5fa" strokeWidth="3" />
          <path d="M 210 128 Q 255 128 265 95" stroke="#60a5fa" strokeWidth="2.5" fill="none" strokeLinecap="round" markerEnd="url(#arr)" />

          {/* ── Garden bed ── */}
          <rect x="265" y="82" width="200" height="108" rx="8" fill="#86efac" opacity="0.18" stroke="#4ade80" strokeWidth="1" />
          {/* Soil */}
          <rect x="265" y="155" width="200" height="35" rx="6" fill="#92400e" opacity="0.35" />

          {/* Plants */}
          {[295, 335, 375, 415, 450].map((x, i) => (
            <g key={i} fill="none" stroke="#16a34a" strokeWidth="1.5">
              <path d={`M ${x} 155 Q ${x} ${138 - i * 2} ${x - 10} ${126 - i * 2}`} />
              <path d={`M ${x} ${145 - i} Q ${x + 8} ${140 - i} ${x + 14} ${132 - i}`} />
              <ellipse cx={x - 11} cy={124 - i * 2} rx="7" ry="5" fill="#4ade80" opacity="0.75" stroke="none" />
              <ellipse cx={x + 15} cy={130 - i} rx="6" ry="4" fill="#4ade80" opacity="0.75" stroke="none" />
            </g>
          ))}

          {/* Drip drops */}
          <g fill="#38bdf8" opacity="0.7">
            {[308, 347, 386, 424, 458].map((x, i) => (
              <ellipse key={i} cx={x} cy="157" rx="2" ry="3" />
            ))}
          </g>

          {/* Irrigation header pipe */}
          <line x1="265" y1="90" x2="465" y2="90" stroke="#60a5fa" strokeWidth="2" opacity="0.55" />
          {/* Drippers */}
          <g stroke="#38bdf8" strokeWidth="1" opacity="0.45">
            {[300, 340, 380, 420, 455].map((x, i) => (
              <line key={i} x1={x} y1="90" x2={x} y2="108" />
            ))}
          </g>

          {/* ── Moisture sensor card: M1 top-left ── */}
          <rect x="265" y="10" width="92" height="46" rx="6" fill="#f0fdf4" stroke="#4ade80" strokeWidth="1" />
          <circle cx="283" cy="33" r="10" fill="#bbf7d0" stroke="#4ade80" strokeWidth="1" />
          <path d="M283 26 Q287 32 283 38 Q279 32 283 26Z" fill="#22c55e" opacity="0.85" />
          <text x="298" y="27" fontSize="9" fill="#15803d" fontWeight="500">Moisture 1</text>
          <rect x="298" y="30" width="52" height="5" rx="2" fill="#dcfce7" />
          <rect x="298" y="30" width={52 * m1.pct / 100} height="5" rx="2" fill="#4ade80" />
          <text x="298" y="48" fontSize="9" fill="#16a34a">{m1.label}</text>
          <line x1="311" y1="56" x2="311" y2="82" stroke="#4ade80" strokeWidth="1" strokeDasharray="3,2" />

          {/* ── Moisture sensor card: M2 top-right ── */}
          <rect x="373" y="10" width="92" height="46" rx="6" fill="#f0fdf4" stroke="#4ade80" strokeWidth="1" />
          <circle cx="391" cy="33" r="10" fill="#bbf7d0" stroke="#4ade80" strokeWidth="1" />
          <path d="M391 26 Q395 32 391 38 Q387 32 391 26Z" fill="#22c55e" opacity="0.85" />
          <text x="406" y="27" fontSize="9" fill="#15803d" fontWeight="500">Moisture 2</text>
          <rect x="406" y="30" width="52" height="5" rx="2" fill="#dcfce7" />
          <rect x="406" y="30" width={52 * m2.pct / 100} height="5" rx="2" fill="#4ade80" />
          <text x="406" y="48" fontSize="9" fill="#16a34a">{m2.label}</text>
          <line x1="419" y1="56" x2="419" y2="82" stroke="#4ade80" strokeWidth="1" strokeDasharray="3,2" />

          {/* ── Moisture sensor card: M3 bottom-left ── */}
          <rect x="265" y="204" width="92" height="46" rx="6" fill="#f0fdf4" stroke="#4ade80" strokeWidth="1" />
          <circle cx="283" cy="227" r="10" fill="#bbf7d0" stroke="#4ade80" strokeWidth="1" />
          <path d="M283 220 Q287 226 283 234 Q279 226 283 220Z" fill="#22c55e" opacity="0.85" />
          <text x="298" y="221" fontSize="9" fill="#15803d" fontWeight="500">Moisture 3</text>
          <rect x="298" y="224" width="52" height="5" rx="2" fill="#dcfce7" />
          <rect x="298" y="224" width={52 * m3.pct / 100} height="5" rx="2" fill="#4ade80" />
          <text x="298" y="242" fontSize="9" fill="#16a34a">{m3.label}</text>
          <line x1="311" y1="190" x2="311" y2="204" stroke="#4ade80" strokeWidth="1" strokeDasharray="3,2" />

          {/* ── Moisture sensor card: M4 bottom-right ── */}
          <rect x="373" y="204" width="92" height="46" rx="6" fill="#f0fdf4" stroke="#4ade80" strokeWidth="1" />
          <circle cx="391" cy="227" r="10" fill="#bbf7d0" stroke="#4ade80" strokeWidth="1" />
          <path d="M391 220 Q395 226 391 234 Q387 226 391 220Z" fill="#22c55e" opacity="0.85" />
          <text x="406" y="221" fontSize="9" fill="#15803d" fontWeight="500">Moisture 4</text>
          <rect x="406" y="224" width="52" height="5" rx="2" fill="#dcfce7" />
          <rect x="406" y="224" width={52 * m4.pct / 100} height="5" rx="2" fill="#4ade80" />
          <text x="406" y="242" fontSize="9" fill="#16a34a">{m4.label}</text>
          <line x1="419" y1="190" x2="419" y2="204" stroke="#4ade80" strokeWidth="1" strokeDasharray="3,2" />

          {/* ── Animated water flow dot ── */}
          {hasWaterFlow && (
            <circle r="4" fill="#38bdf8" opacity="0.9">
              <animateMotion
                dur="2s"
                repeatCount="indefinite"
                path="M 170 99 Q 210 99 210 128 Q 255 128 265 95"
              />
            </circle>
          )}
        </svg>

        {/* Flow sensor labels */}
        <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 px-3 py-2 text-gray-700 dark:text-gray-300">
            Flow Sensor 1:{' '}
            <span className="font-semibold text-cyan-600 dark:text-cyan-400">
              {sensorData.flowSensor1 ?? 'N/A'} L / Min
            </span>
          </div>
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 px-3 py-2 text-gray-700 dark:text-gray-300">
            Flow Sensor 2:{' '}
            <span className="font-semibold text-cyan-600 dark:text-cyan-400">
              {sensorData.flowSensor2 ?? 'N/A'} L / Min
            </span>
          </div>
        </div>
      </div>
    );
  };

  // ─── Render ─────────────────────────────────────────────────────────────────

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">Dashboard</h1>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <div
                className={`w-3 h-3 rounded-full ${
                  isConnected ? 'bg-green-500' : 'bg-red-500'
                } animate-pulse`}
              />
              <span className="text-sm text-gray-600 dark:text-gray-400">
                {isConnected ? 'Connected' : 'Disconnected'}
              </span>
            </div>
            {isConnected && lastResponseTime && (
              <span className="text-sm text-gray-500 dark:text-gray-400">
                Response time: {lastResponseTime}ms
              </span>
            )}
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="mb-6 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Loading */}
        {loading && !error && (
          <div className="mb-6 text-center py-8">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600 dark:border-green-400" />
            <p className="mt-2 text-gray-600 dark:text-gray-400">Loading sensor data...</p>
          </div>
        )}

        {/* Irrigation Flow Visual */}
        <IrrigationVisual />

        {/* Environmental Row */}
        <div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <SensorCard
              title="Temperature"
              value={sensorData.temperature !== null ? (sensorData.temperature / 10).toFixed(1) : null}
              unit="°C"
              icon="🌡️"
            />
            <SensorCard
              title="Humidity"
              value={sensorData.humidity !== null ? (sensorData.humidity / 10).toFixed(1) : null}
              unit="%"
              icon="💧"
            />
            <SensorCard
              title="Light Intensity"
              value={sensorData.lightIntensity}
              unit="%"
              icon="☀️"
            />
          </div>
        </div>

        {/* Soil Moisture Row */}
        <div className="mt-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <SensorCard title="Soil Moisture 1" value={sensorData.soilMoisture1} unit="%" icon="🌱" />
            <SensorCard title="Soil Moisture 2" value={sensorData.soilMoisture2} unit="%" icon="🌱" />
            <SensorCard title="Soil Moisture 3" value={sensorData.soilMoisture3} unit="%" icon="🌱" />
            <SensorCard title="Soil Moisture 4" value={sensorData.soilMoisture4} unit="%" icon="🌱" />
          </div>
        </div>

        {/* Water System Row */}
        <div className="mt-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="md:col-span-1">
              <SensorCard title="Water Level" value={sensorData.waterLevel} unit="%" icon="💦" />
            </div>
            <SensorCard title="Flow Sensor 1" value={sensorData.flowSensor1} unit="L / Min" icon="🚰" />
            <SensorCard title="Flow Sensor 2" value={sensorData.flowSensor2} unit="L / Min" icon="🚰" />
          </div>
        </div>

      </div>
    </div>
  );
}