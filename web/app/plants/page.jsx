'use client';

import { useState, useEffect } from 'react';
import { sendPlantProfile } from '@/lib/espApi';
import { useEspConnection } from '@/lib/useEspConnection';
import { getMockPlantData } from '@/lib/mockPlantData';

export default function Plants() {
  const { isConnected } = useEspConnection();
  const [profiles, setProfiles] = useState([]);
  const [activeProfileId, setActiveProfileId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: null, text: '' });
  const [showForm, setShowForm] = useState(false);
  const [editingProfile, setEditingProfile] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    timesPerDay: 1,
    timeOfDay: ['08:00'],
    waterAmount: 500,
  });

  // Load profiles from localStorage on mount
  useEffect(() => {
    const loadData = async () => {
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
          const mockData = await getMockPlantData();
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

  const saveProfiles = (newProfiles) => {
    try {
      localStorage.setItem('plantProfiles', JSON.stringify(newProfiles));
      setProfiles(newProfiles);
    } catch (error) {
      console.error('Error saving profiles:', error);
      setMessage({ type: 'error', text: 'Failed to save profile' });
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!formData.name.trim()) {
      setMessage({ type: 'error', text: 'Plant name is required' });
      return;
    }

    if (formData.timesPerDay < 1 || formData.timesPerDay > 10) {
      setMessage({ type: 'error', text: 'Times per day must be between 1 and 10' });
      return;
    }

    if (formData.waterAmount < 1) {
      setMessage({ type: 'error', text: 'Water amount must be greater than 0' });
      return;
    }

    const newProfiles = [...profiles];
    
    if (editingProfile) {
      // Update existing profile
      const index = newProfiles.findIndex(p => p.id === editingProfile.id);
      if (index !== -1) {
        newProfiles[index] = { ...editingProfile, ...formData };
      }
    } else {
      // Create new profile
      const newProfile = {
        id: Date.now().toString(),
        ...formData,
      };
      newProfiles.push(newProfile);
    }

    saveProfiles(newProfiles);
    resetForm();
    setMessage({ type: 'success', text: editingProfile ? 'Profile updated!' : 'Profile created!' });
  };

  const resetForm = () => {
    setFormData({
      name: '',
      timesPerDay: 1,
      timeOfDay: ['08:00'],
      waterAmount: 500,
    });
    setEditingProfile(null);
    setShowForm(false);
  };

  const handleEdit = (profile) => {
    setEditingProfile(profile);
    setFormData({
      name: profile.name,
      timesPerDay: profile.timesPerDay,
      timeOfDay: [...profile.timeOfDay],
      waterAmount: profile.waterAmount,
    });
    setShowForm(true);
  };

  const handleDelete = (id) => {
    if (confirm('Are you sure you want to delete this profile?')) {
      const newProfiles = profiles.filter(p => p.id !== id);
      saveProfiles(newProfiles);
      
      if (activeProfileId === id) {
        setActiveProfileId(null);
        localStorage.removeItem('activePlantProfile');
      }
      
      setMessage({ type: 'success', text: 'Profile deleted!' });
    }
  };

  const handleSetActive = async (profile) => {
    setActiveProfileId(profile.id);
    localStorage.setItem('activePlantProfile', profile.id);
    
    if (isConnected) {
      setLoading(true);
      setMessage({ type: null, text: '' });
      
      try {
        await sendPlantProfile(profile);
        setMessage({ type: 'success', text: 'Active profile sent to ESP32!' });
      } catch (error) {
        setMessage({ type: 'error', text: error.message || 'Failed to send profile to ESP32' });
      } finally {
        setLoading(false);
      }
    } else {
      setMessage({ type: 'warning', text: 'Profile set as active, but ESP32 is not connected. Will send when connected.' });
    }
  };

  const handleTimeChange = (index, value) => {
    const newTimes = [...formData.timeOfDay];
    newTimes[index] = value;
    setFormData({ ...formData, timeOfDay: newTimes });
  };

  const handleTimesPerDayChange = (value) => {
    const times = parseInt(value) || 1;
    setFormData({
      ...formData,
      timesPerDay: times,
      timeOfDay: Array(times).fill('').map((_, i) => formData.timeOfDay[i] || '08:00'),
    });
  };

  const activeProfile = profiles.find(p => p.id === activeProfileId);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Plant Profiles</h1>
            <button
              onClick={() => {
                resetForm();
                setShowForm(true);
              }}
              className="px-6 py-3 bg-green-600 dark:bg-green-500 text-white rounded-lg font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors shadow-md hover:shadow-lg"
            >
              + New Profile
            </button>
          </div>
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
            {activeProfile && (
              <span className="text-sm text-green-600 dark:text-green-400 font-medium">
                Active: {activeProfile.name}
              </span>
            )}
          </div>
        </div>

        {/* Message Display */}
        {message.type && (
          <div
            className={`mb-6 px-4 py-3 rounded-lg ${
              message.type === 'success'
                ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-800 dark:text-green-400'
                : message.type === 'warning'
                ? 'bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 text-yellow-800 dark:text-yellow-400'
                : 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-800 dark:text-red-400'
            }`}
          >
            {message.text}
          </div>
        )}

        {/* Form */}
        {showForm && (
          <div className="mb-8 bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border border-gray-200 dark:border-gray-700">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-6">
              {editingProfile ? 'Edit Profile' : 'New Plant Profile'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Plant Name
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="e.g., Tomato Plant"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Times Per Day
                </label>
                <input
                  type="number"
                  min="1"
                  max="10"
                  value={formData.timesPerDay}
                  onChange={(e) => handleTimesPerDayChange(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Time of Day
                </label>
                <div className="space-y-2">
                  {formData.timeOfDay.map((time, index) => (
                    <input
                      key={index}
                      type="time"
                      value={time}
                      onChange={(e) => handleTimeChange(index, e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      required
                    />
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Water Amount (ml)
                </label>
                <input
                  type="number"
                  min="1"
                  value={formData.waterAmount}
                  onChange={(e) => setFormData({ ...formData, waterAmount: parseInt(e.target.value) || 0 })}
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  required
                />
              </div>

              <div className="flex gap-4">
                <button
                  type="submit"
                  className="flex-1 px-6 py-3 bg-green-600 dark:bg-green-500 text-white rounded-lg font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors shadow-md hover:shadow-lg"
                >
                  {editingProfile ? 'Update Profile' : 'Create Profile'}
                </button>
                <button
                  type="button"
                  onClick={resetForm}
                  className="flex-1 px-6 py-3 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Profiles List */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {profiles.map((profile) => (
            <div
              key={profile.id}
              className={`bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border-2 transition-all ${
                activeProfileId === profile.id
                  ? 'border-green-500 dark:border-green-400 shadow-lg'
                  : 'border-gray-200 dark:border-gray-700'
              }`}
            >
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-1">
                    {profile.name}
                  </h3>
                  {activeProfileId === profile.id && (
                    <span className="inline-block px-2 py-1 text-xs font-semibold bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400 rounded">
                      Active
                    </span>
                  )}
                </div>
              </div>

              <div className="space-y-3 mb-4">
                <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                  <span>🕐</span>
                  <span>{profile.timesPerDay} time{profile.timesPerDay > 1 ? 's' : ''} per day</span>
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">
                  <div className="font-medium mb-1">Times:</div>
                  <div className="flex flex-wrap gap-2">
                    {profile.timeOfDay.map((time, idx) => (
                      <span
                        key={idx}
                        className="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded text-xs"
                      >
                        {time}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                  <span>💧</span>
                  <span>{profile.waterAmount} ml per watering</span>
                </div>
              </div>

              <div className="flex gap-2">
                {activeProfileId !== profile.id && (
                  <button
                    onClick={() => handleSetActive(profile)}
                    disabled={loading}
                    className="flex-1 px-4 py-2 bg-green-600 dark:bg-green-500 text-white rounded-lg text-sm font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Set Active
                  </button>
                )}
                <button
                  onClick={() => handleEdit(profile)}
                  className="px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg text-sm font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(profile.id)}
                  className="px-4 py-2 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded-lg text-sm font-semibold hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>

        {profiles.length === 0 && !showForm && (
          <div className="text-center py-12 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
            <p className="text-gray-600 dark:text-gray-400 mb-4">No plant profiles yet</p>
            <button
              onClick={() => setShowForm(true)}
              className="px-6 py-3 bg-green-600 dark:bg-green-500 text-white rounded-lg font-semibold hover:bg-green-700 dark:hover:bg-green-600 transition-colors"
            >
              Create Your First Profile
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

