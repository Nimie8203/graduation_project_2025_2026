'use client';

import { useState } from 'react';
import { startPump, stopPump } from '@/lib/espApi';
import { useEspConnection } from '@/lib/useEspConnection';

export default function ManualControl() {
  const { isConnected } = useEspConnection();
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: null, text: '' });

  const handleStartPump = async () => {
    if (!isConnected) {
      setMessage({ type: 'error', text: 'ESP32 is not connected' });
      return;
    }

    setLoading(true);
    setMessage({ type: null, text: '' });

    try {
      const response = await startPump();
      setMessage({
        type: 'success',
        text: 'Pump started successfully',
      });
    } catch (error) {
      setMessage({
        type: 'error',
        text: error.message || 'Failed to start pump',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleStopPump = async () => {
    if (!isConnected) {
      setMessage({ type: 'error', text: 'ESP32 is not connected' });
      return;
    }

    setLoading(true);
    setMessage({ type: null, text: '' });

    try {
      const response = await stopPump();
      setMessage({
        type: 'success',
        text: 'Pump stopped successfully',
      });
    } catch (error) {
      setMessage({
        type: 'error',
        text: error.message || 'Failed to stop pump',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">Manual Control</h1>
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
          </div>
        </div>

        {/* Connection Warning */}
        {!isConnected && (
          <div className="mb-6 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 text-yellow-800 dark:text-yellow-400 px-4 py-3 rounded-lg">
            ⚠️ ESP32 is not connected. Controls are disabled.
          </div>
        )}

        {/* Message Display */}
        {message.type && (
          <div
            className={`mb-6 px-4 py-3 rounded-lg ${
              message.type === 'success'
                ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-800 dark:text-green-400'
                : 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-800 dark:text-red-400'
            }`}
          >
            {message.text}
          </div>
        )}

        {/* Control Panel */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-8 border border-gray-200 dark:border-gray-700">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-6">Pump Control</h2>
          
          <div className="flex flex-col sm:flex-row gap-4">
            <button
              onClick={handleStartPump}
              disabled={!isConnected || loading}
              className={`flex-1 px-6 py-4 rounded-lg font-semibold text-white transition-all ${
                !isConnected || loading
                  ? 'bg-gray-400 dark:bg-gray-600 cursor-not-allowed'
                  : 'bg-green-600 dark:bg-green-500 hover:bg-green-700 dark:hover:bg-green-600 active:bg-green-800 dark:active:bg-green-700 shadow-md hover:shadow-lg'
              }`}
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></span>
                  Processing...
                </span>
              ) : (
                <span className="flex items-center justify-center gap-2">
                  <span>🚰</span>
                  Start Pump
                </span>
              )}
            </button>

            <button
              onClick={handleStopPump}
              disabled={!isConnected || loading}
              className={`flex-1 px-6 py-4 rounded-lg font-semibold text-white transition-all ${
                !isConnected || loading
                  ? 'bg-gray-400 dark:bg-gray-600 cursor-not-allowed'
                  : 'bg-red-600 dark:bg-red-500 hover:bg-red-700 dark:hover:bg-red-600 active:bg-red-800 dark:active:bg-red-700 shadow-md hover:shadow-lg'
              }`}
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></span>
                  Processing...
                </span>
              ) : (
                <span className="flex items-center justify-center gap-2">
                  <span>⏹️</span>
                  Stop Pump
                </span>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

