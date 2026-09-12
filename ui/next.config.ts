import type { NextConfig } from "next";

const GRAPHQL_URL = process.env.GRAPHQL_URL || "http://localhost:8080/graphql";

const nextConfig: NextConfig = {
  async headers() {
    return [
      {
        source: "/:path*",
        headers: [
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
          { key: "X-Frame-Options", value: "SAMEORIGIN" },
          { key: "Link", value: "</sitemap.xml>; rel=sitemap" },
        ],
      },
    ];
  },
  async rewrites() {
    return [
      {
        source: "/api/graphql/ws",
        destination: GRAPHQL_URL,
      },
    ];
  },
};

export default nextConfig;
