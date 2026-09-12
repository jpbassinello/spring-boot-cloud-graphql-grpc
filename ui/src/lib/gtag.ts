// Google Ads (gtag.js) integration. The base tag is loaded once in the root layout
// (production only); it auto-captures the gclid on ad-landing pages so conversions fired
// later in the session attribute back to the click.

export const GADS_ID = process.env.NEXT_PUBLIC_GADS_ID ?? "";

// Google Ads "Sign Up" conversion-action label. Public by nature (it ships in client JS), but
// account-specific, so it comes from the environment rather than being committed.
const SIGNUP_LABEL = process.env.NEXT_PUBLIC_GADS_SIGNUP_LABEL ?? "";

const SIGNUP_REPORTED_KEY = "signup_reported";

declare global {
  interface Window {
    gtag?: (...args: unknown[]) => void;
    dataLayer?: unknown[];
  }
}

/** True when the base tag should load (real ad ID + production). */
export const GADS_ENABLED = process.env.NODE_ENV === "production" && Boolean(GADS_ID);

function gtagEvent(name: string, params: Record<string, unknown>): void {
  if (typeof window === "undefined" || typeof window.gtag !== "function") return;
  window.gtag("event", name, params);
}

/**
 * Fire the Google Ads "sign up" conversion. Guarded to fire at most once per browser, which
 * approximates a new registration (repeat logins on the same device won't re-fire) without a
 * backend new-vs-returning signal. No-op until NEXT_PUBLIC_GADS_SIGNUP_LABEL is set.
 */
export function reportSignupConversion(): void {
  if (typeof window === "undefined" || !SIGNUP_LABEL) return;
  try {
    if (localStorage.getItem(SIGNUP_REPORTED_KEY)) return;
    localStorage.setItem(SIGNUP_REPORTED_KEY, "1");
  } catch {
    // localStorage blocked (private mode) — still report rather than lose the conversion.
  }
  gtagEvent("conversion", { send_to: `${GADS_ID}/${SIGNUP_LABEL}` });
}
