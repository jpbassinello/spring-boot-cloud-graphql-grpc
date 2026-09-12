import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Dashboard",
  robots: { index: false, follow: false },
};

export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
  return children;
}
