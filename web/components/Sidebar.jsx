'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState, useEffect } from 'react';

export default function Sidebar() {
  const pathname = usePathname();
  const [darkMode, setDarkMode] = useState(false);

  useEffect(() => {
    // Check for saved theme preference or default to dark mode
    const isDark = localStorage.getItem('theme') === 'dark' || 
                   (!localStorage.getItem('theme') && window.matchMedia('(prefers-color-scheme: dark)').matches);
    setDarkMode(isDark);
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, []);

  const toggleTheme = () => {
    const newDarkMode = !darkMode;
    setDarkMode(newDarkMode);
    if (newDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  };

  const navItems = [
    { href: '/dashboard', label: 'Dashboard', icon: '📊' },
    { href: '/manual', label: 'Manual Control', icon: '🎮' },
    { href: '/plants', label: 'Plant Profiles', icon: '🌱' },
  ];

  return (
    <aside className="fixed left-0 top-0 h-full w-64 bg-white dark:bg-gray-900 text-gray-900 dark:text-white shadow-lg z-10 hidden md:block border-r border-gray-200 dark:border-gray-700">
      <div className="flex h-full flex-col">
        <div className="p-6 border-b border-gray-200 dark:border-gray-700">
          <h1 className="text-xl font-bold text-green-600 dark:text-green-400">ESP32 Control</h1>
        </div>
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            {navItems.map((item) => {
              const isActive = pathname === item.href;
              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                      isActive
                        ? 'bg-green-600 dark:bg-green-500 text-white shadow-md'
                        : 'text-gray-700 dark:text-gray-300 hover:bg-green-50 dark:hover:bg-gray-800 hover:text-green-600 dark:hover:text-green-400'
                    }`}
                  >
                    <span className="text-xl">{item.icon}</span>
                    <span className="font-medium">{item.label}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
        <div className="p-4 border-t border-gray-200 dark:border-gray-700">
          <button
            onClick={toggleTheme}
            className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-lg bg-green-50 dark:bg-gray-800 text-green-600 dark:text-green-400 hover:bg-green-100 dark:hover:bg-gray-700 transition-colors"
          >
            <span>{darkMode ? '☀️' : '🌙'}</span>
            <span className="font-medium">{darkMode ? 'Light Mode' : 'Dark Mode'}</span>
          </button>
        </div>
      </div>
    </aside>
  );
}

