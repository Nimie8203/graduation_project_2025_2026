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

  const fetchSensorData = useCallback(async () => {
    if (!isConnected) {
      setError('ESP32 is not connected');
      setLoading(false);
      return;
    }

    try {
      setError(null);
      const data = await getSensors();
      
      // Map the response data to our state
      // Adjust these keys based on your ESP32 API response structure
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
    // Initial fetch
    fetchSensorData();

    // Set up interval to fetch every 3 seconds
    intervalRef.current = setInterval(() => {
      fetchSensorData();
    }, 3000);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [fetchSensorData]);

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

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Loading State */}
        {loading && !error && (
          <div className="mb-6 text-center py-8">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600 dark:border-green-400"></div>
            <p className="mt-2 text-gray-600 dark:text-gray-400">Loading sensor data...</p>
          </div>
        )}

        {/* Sensor Grid */}
        {/* Replace just the "Sensor Grid" section with this */}

      {/* Environmental Row */}
      <div>
        <p className="text-xs uppercase tracking-widest text-gray-400 dark:text-gray-500 mb-3">
          
        </p>
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
      <div>
        <p className="text-xs uppercase tracking-widest text-gray-400 dark:text-gray-500 mb-3">
          
        </p>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          <SensorCard title="Soil Moisture 1" value={sensorData.soilMoisture1} unit="%" icon="🌱" />
          <SensorCard title="Soil Moisture 2" value={sensorData.soilMoisture2} unit="%" icon="🌱" />
          <SensorCard title="Soil Moisture 3" value={sensorData.soilMoisture3} unit="%" icon="🌱" />
          <SensorCard title="Soil Moisture 4" value={sensorData.soilMoisture4} unit="%" icon="🌱" />
        </div>
      </div>

      {/* Water System Row */}
      <div>
        <p className="text-xs uppercase tracking-widest text-gray-400 dark:text-gray-500 mb-3">
          
        </p>
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

