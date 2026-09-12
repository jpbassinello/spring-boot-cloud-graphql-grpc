/**
 * First-touch acquisition attribution.
 *
 * UTM params on an inbound link are captured into a first-party cookie by the proxy on the
 * visitor's first landing. The cookie survives the whole sign-in / OAuth flow (same domain), so
 * a sign-in mutation can read it back here and forward it to the gateway, which persists it only
 * when the sign-in creates a new account.
 */

export type Attribution = {
  channel?: string; // utm_source
  theme?: string; // utm_term — content theme
  variant?: string; // utm_content — placement or A/B cell
  utm?: string; // raw utm query string
};

export const ATTRIBUTION_COOKIE = "attribution";
export const ATTRIBUTION_MAX_AGE_SECONDS = 60 * 60 * 24 * 30; // 30 days

/**
 * Builds an {@link Attribution} from query params, keeping the raw utm string for anything the
 * three semantic fields don't capture. Returns null when no utm params are present — so the
 * proxy only sets the cookie for genuinely attributed visits.
 */
export function attributionFromParams(params: URLSearchParams): Attribution | null {
  const channel = params.get("utm_source") ?? undefined;
  const theme = params.get("utm_term") ?? undefined;
  const variant = params.get("utm_content") ?? undefined;

  const utmParts: string[] = [];
  params.forEach((value, key) => {
    if (key.startsWith("utm_")) utmParts.push(`${key}=${value}`);
  });
  if (utmParts.length === 0) return null;

  const attribution: Attribution = {};
  if (channel) attribution.channel = channel;
  if (theme) attribution.theme = theme;
  if (variant) attribution.variant = variant;
  attribution.utm = utmParts.join("&");
  return attribution;
}

/** Reads the captured attribution cookie (client-side), or null if absent/invalid/empty. */
export function readAttribution(): Attribution | null {
  if (typeof document === "undefined") return null;

  const entry = document.cookie.split("; ").find((c) => c.startsWith(`${ATTRIBUTION_COOKIE}=`));
  if (!entry) return null;

  try {
    const parsed = JSON.parse(
      decodeURIComponent(entry.slice(ATTRIBUTION_COOKIE.length + 1)),
    ) as Attribution;

    const out: Attribution = {};
    if (parsed.channel) out.channel = parsed.channel;
    if (parsed.theme) out.theme = parsed.theme;
    if (parsed.variant) out.variant = parsed.variant;
    if (parsed.utm) out.utm = parsed.utm;
    return Object.keys(out).length > 0 ? out : null;
  } catch {
    return null;
  }
}
