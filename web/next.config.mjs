import path from "path";
import { fileURLToPath } from "url";

/** Directory containing this config (the Next.js app root). */
const projectRoot = path.dirname(fileURLToPath(import.meta.url));

const tailwindPkg = path.join(projectRoot, "node_modules", "tailwindcss");

/** @type {import('next').NextConfig} */
const nextConfig = {
  turbopack: {
    root: projectRoot,
    // When the repo lives under a parent folder without its own package.json, bare
    // `tailwindcss` imports must still resolve to this app's node_modules.
    resolveAlias: {
      tailwindcss: tailwindPkg,
    },
  },
};

export default nextConfig;
