# ui — Next.js frontend

The client-facing app. It talks to the GraphQL gateway and nothing else: no service in
`services/grpc/` is reachable from a browser.

## Stack

- **Next.js (App Router)** + React, TypeScript, Tailwind v4
- **urql** (`@urql/next`) for queries/mutations, `graphql-ws` for subscriptions
- **graphql-codegen** client preset — typed documents generated from the gateway's `.graphqls`
- **shadcn/ui** primitives under `src/components/ui/` (`components.json` holds the generator config)
- **pnpm**

## Commands

```bash
pnpm install
pnpm codegen        # regenerate src/graphql/generated/ from the gateway schema
pnpm dev            # http://localhost:3000
pnpm build
pnpm lint
pnpm format
```

`src/graphql/generated/` is produced by `pnpm codegen` and is not committed — run it once after
cloning, and again whenever you change a `.graphqls` file or a query in a `.tsx` file. Codegen
reads the gateway's schema files straight off disk
(`../services/graphql/gateway/src/main/resources/graphql/*.graphqls`), so no server needs to be
running, and a schema change that breaks a query becomes a type error rather than a runtime one.

## Environment

| Variable                        | Default                       | Used by                                     |
|---------------------------------|-------------------------------|---------------------------------------------|
| `GRAPHQL_URL`                   | `http://localhost:8080/graphql` | server-side: the `/api/graphql` proxy route |
| `NEXT_PUBLIC_GRAPHQL_URL`       | `http://localhost:8080/graphql` | browser: urql client + WebSocket URL        |
| `NEXT_PUBLIC_SITE_URL`          | `http://localhost:3000`       | canonical URLs, `robots.txt`, `sitemap.xml` |
| `NEXT_PUBLIC_GADS_ID`           | _unset_ (disabled)            | Google Ads base tag (production only)       |
| `NEXT_PUBLIC_GADS_SIGNUP_LABEL` | _unset_ (no-op)               | Google Ads sign-up conversion               |

Put local values in `.env.local` (gitignored).

## What's here

- `src/lib/urql/provider.tsx` — the urql client. Note the WebSocket keep-alive and pong
  watchdog: a laptop returning from sleep leaves a half-open socket where no close event ever
  arrives, so a client that only *answers* pings waits on a dead connection forever with every
  subscription silently stalled. Pinging outbound is what surfaces the break; the watchdog
  closes the socket so the retry logic can run.
- `src/app/api/graphql/route.ts` — server-side proxy for calls that should not expose the
  gateway URL to the browser.
- `src/lib/auth.ts`, `src/lib/use-require-auth.ts`, `src/lib/next-url.ts` — bearer token in
  `localStorage`, expiry check, redirect-to-sign-in with a safe `next` parameter.
- `src/proxy.ts` + `src/lib/attribution.ts` — first-touch UTM capture into a first-party cookie,
  so the channel that originally brought a visitor in survives the whole sign-in flow and can be
  attached to the account the gateway creates.
- `src/lib/gtag.ts` — Google Ads tag, off unless the env vars are set.
- `netlify.toml` — HSTS plus long-lived caching for fingerprinted assets. The other security and
  crawl headers live in `next.config.ts`'s `headers()`, because Next's SSR/edge-rendered routes
  don't pick them up from Netlify's config — only static-asset responses do.

## Not here

There is no sign-in page: this scaffold's gateway exposes `registerUser` /
`verifyUserContactMethod` and expects a Keycloak-issued JWT, so the authentication UI depends on
which flow you choose. The forks of this project (`jobs-ai`, `growth-console`) implement a
passwordless email-code flow plus LinkedIn and Google OAuth as
`src/app/(auth)/{sign-in,verify-code,auth/*}` against gateway mutations of their own — copy from
there if you want that shape, along with `src/lib/linkedin.ts` / `src/lib/google.ts`.
