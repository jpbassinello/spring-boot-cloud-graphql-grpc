import type { MetadataRoute } from "next";

export default function robots(): MetadataRoute.Robots {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:3000";

  return {
    rules: [
      {
        userAgent: "*",
        allow: "/",
        // Signed-in areas only. Nothing a link-preview crawler needs may live under a
        // disallowed path: facebookexternalhit (WhatsApp, Messenger, Facebook), Twitterbot,
        // Slackbot and LinkedInBot all honour robots.txt and will skip an og:image they are
        // not allowed to fetch.
        disallow: ["/api/", "/dashboard/"],
      },
    ],
    sitemap: `${baseUrl}/sitemap.xml`,
  };
}
