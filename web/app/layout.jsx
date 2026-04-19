import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Sidebar from "@/components/Sidebar";
import MobileNav from "@/components/MobileNav";
import ThemeScript from "@/components/ThemeScript";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata = {
  title: "ESP32 Control Dashboard",
  description: "ESP32 sensor monitoring and control system",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <ThemeScript />
        <div className="flex min-h-screen">
          <Sidebar />
          <main className="flex-1 pb-[4.5rem] md:ml-64 md:pb-0">
            {children}
          </main>
          <MobileNav />
        </div>
      </body>
    </html>
  );
}
