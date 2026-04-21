'use client';

import { useState, useEffect, useRef } from 'react';

const ESP32_BASE_URL = 'http://192.168.4.1';
const PING_INTERVAL = 10000; // Ping every 3 seconds
const PING_TIMEOUT = 9000; // 2 second timeout for ping
const PING_ENDPOINT = '/api/status'; // Adjust this endpoint based on your ESP32 API

/**
 * Custom React hook to monitor connection status with ESP32
 * @param {number} interval - Ping interval in milliseconds (default: 3000)
 * @returns {Object} - { isConnected, lastResponseTime }
 */
export function useEspConnection(interval = PING_INTERVAL) {
  const [isConnected, setIsConnected] = useState(false);
  const [lastResponseTime, setLastResponseTime] = useState(null);
  const intervalRef = useRef(null);
  const mountedRef = useRef(true);

  const pingEsp32 = async () => {
    const startTime = Date.now();
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), PING_TIMEOUT);

    try {
      const response = await fetch(`${ESP32_BASE_URL}${PING_ENDPOINT}`, {
        method: 'GET',
        signal: controller.signal,
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

  useEffect(() => {
    mountedRef.current = true;
    
    // Initial ping
    pingEsp32();

    // Set up interval for periodic pings
    intervalRef.current = setInterval(() => {
      pingEsp32();
    }, interval);

    // Cleanup function
    return () => {
      mountedRef.current = false;
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [interval]);

  return {
    isConnected,
    lastResponseTime,
  };
}

