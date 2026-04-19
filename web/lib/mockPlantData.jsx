// Mock plant data for demo purposes
// This simulates plant data that would come from ESP32

export const mockPlantData = {
  plants: [
    {
      id: '1',
      name: 'Tomato Plant',
      timesPerDay: 2,
      timeOfDay: ['08:00', '18:00'],
      waterAmount: 500,
    },
    {
      id: '2',
      name: 'Basil',
      timesPerDay: 1,
      timeOfDay: ['09:00'],
      waterAmount: 300,
    },
    {
      id: '3',
      name: 'Lettuce',
      timesPerDay: 3,
      timeOfDay: ['07:00', '13:00', '19:00'],
      waterAmount: 400,
    },
  ],
};

/**
 * Simulates fetching plant data from ESP32
 * In production, this would be replaced with actual API call
 */
export async function getMockPlantData() {
  // Simulate API delay
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockPlantData);
    }, 500);
  });
}

