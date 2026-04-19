"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const items = [
  { href: "/dashboard", label: "Dashboard", icon: "📊" },
  { href: "/manual", label: "Manual", icon: "🎮" },
  { href: "/plants", label: "Plants", icon: "🌱" },
];

export default function MobileNav() {
  const pathname = usePathname();

  return (
    <nav
      className="fixed bottom-0 left-0 right-0 z-20 flex border-t border-gray-200 bg-white/95 backdrop-blur dark:border-gray-700 dark:bg-gray-900/95 md:hidden"
      aria-label="Mobile navigation"
    >
      {items.map((item) => {
        const active = pathname === item.href;
        return (
          <Link
            key={item.href}
            href={item.href}
            prefetch
            className={`flex flex-1 flex-col items-center justify-center gap-0.5 py-2 text-[11px] font-medium ${
              active
                ? "text-green-600 dark:text-green-400"
                : "text-gray-600 dark:text-gray-400"
            }`}
          >
            <span className="text-lg leading-none" aria-hidden>
              {item.icon}
            </span>
            <span>{item.label}</span>
          </Link>
        );
      })}
    </nav>
  );
}
