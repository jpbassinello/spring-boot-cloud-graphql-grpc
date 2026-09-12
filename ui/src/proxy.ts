import { NextResponse, type NextRequest } from "next/server";

import {
  ATTRIBUTION_COOKIE,
  ATTRIBUTION_MAX_AGE_SECONDS,
  attributionFromParams,
} from "@/lib/attribution";

/**
 * Captures first-touch acquisition attribution: on the visitor's first landing carrying UTM
 * params, store them in a first-party cookie. First-touch = we never overwrite an existing
 * cookie, so the channel/theme that originally brought the visitor in is what gets persisted at
 * signup. The cookie is intentionally not HttpOnly so the client-side sign-in mutations can read
 * it back.
 */
export function proxy(request: NextRequest) {
  const response = NextResponse.next();

  if (request.cookies.has(ATTRIBUTION_COOKIE)) {
    return response;
  }

  const attribution = attributionFromParams(request.nextUrl.searchParams);
  if (!attribution) {
    return response;
  }

  response.cookies.set(ATTRIBUTION_COOKIE, JSON.stringify(attribution), {
    maxAge: ATTRIBUTION_MAX_AGE_SECONDS,
    path: "/",
    sameSite: "lax",
    httpOnly: false,
  });
  return response;
}

export const config = {
  // Run on page navigations only — skip API routes, Next internals, and static assets.
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico|.*\\..*).*)"],
};
