"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { clearAccessToken, getAccessToken, isTokenExpired } from "@/lib/auth";
import { signInUrl } from "@/lib/next-url";

export function useRequireAuth(): void {
  const router = useRouter();

  useEffect(() => {
    const token = getAccessToken();
    if (token && !isTokenExpired(token)) return;
    clearAccessToken();
    const next = `${window.location.pathname}${window.location.search}`;
    router.replace(signInUrl(next));
  }, [router]);
}
