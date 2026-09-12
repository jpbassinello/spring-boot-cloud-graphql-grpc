export const AFTER_AUTH_DEFAULT = "/dashboard";

export function safeNextUrl(
  next: string | null | undefined,
  fallback: string = AFTER_AUTH_DEFAULT,
): string {
  if (!next) return fallback;
  if (!next.startsWith("/")) return fallback;
  if (next.startsWith("//") || next.startsWith("/\\")) return fallback;
  return next;
}

export function signInUrl(next?: string | null): string {
  if (!next) return "/sign-in";
  return `/sign-in?next=${encodeURIComponent(next)}`;
}
