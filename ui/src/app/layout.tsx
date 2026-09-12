import type { Metadata, Viewport } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Script from "next/script";
import { GraphQLProvider } from "@/lib/urql/provider";
import { GADS_ENABLED, GADS_ID } from "@/lib/gtag";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

const siteName = "SBCGG";
const siteTitle = "SBCGG — Spring Boot Cloud GraphQL gRPC";
// Kept under ~160 characters: Google truncates the snippet past that, and WhatsApp/LinkedIn
// cards cut off around 120.
const siteDescription =
  "A microservices reference stack: Spring Boot, Consul, Keycloak, gRPC between services and " +
  "a GraphQL gateway at the edge.";

export const metadata: Metadata = {
  title: {
    default: siteTitle,
    template: `%s | ${siteName}`,
  },
  description: siteDescription,
  authors: [{ name: siteName }],
  creator: siteName,
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:3000"),
  // No `images` here: the app/opengraph-image.tsx file convention supplies them. An explicit
  // `images` array would override that convention.
  openGraph: {
    type: "website",
    locale: "en_US",
    siteName,
    url: "/",
    title: siteTitle,
    description: siteDescription,
  },
  twitter: {
    card: "summary_large_image",
    title: siteTitle,
    description: siteDescription,
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
  alternates: {
    canonical: "/",
  },
};

export const viewport: Viewport = {
  themeColor: "#0A0E14",
  width: "device-width",
  initialScale: 1,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        {GADS_ENABLED && (
          <>
            {/*
              beforeInteractive injects the tag into the initial server-rendered HTML so
              Google Ads' verification crawler (which doesn't wait for deferred client
              scripts) can detect it. afterInteractive still fires for real users, but the
              scanner reports the tag as "not verified" when the snippet is added
              post-hydration.
            */}
            <Script
              src={`https://www.googletagmanager.com/gtag/js?id=${GADS_ID}`}
              strategy="beforeInteractive"
            />
            <Script id="gtag-init" strategy="beforeInteractive">
              {`window.dataLayer = window.dataLayer || [];
function gtag(){dataLayer.push(arguments);}
gtag('js', new Date());
gtag('config', '${GADS_ID}');`}
            </Script>
          </>
        )}
        <GraphQLProvider>{children}</GraphQLProvider>
      </body>
    </html>
  );
}
