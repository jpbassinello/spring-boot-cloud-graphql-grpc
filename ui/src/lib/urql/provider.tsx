"use client";

import { ssrExchange, UrqlProvider } from "@urql/next";
import { cacheExchange, createClient, fetchExchange, subscriptionExchange } from "urql";
import { createClient as createWSClient } from "graphql-ws";
import { useMemo } from "react";
import { getAccessToken } from "@/lib/auth";

const GRAPHQL_URL = process.env.NEXT_PUBLIC_GRAPHQL_URL || "http://localhost:8081/graphql";

const WS_URL = typeof window !== "undefined" ? GRAPHQL_URL.replace(/^http/, "ws") : "";

/** How long the socket may sit idle before we ping it to prove it is still there. */
const KEEP_ALIVE_MS = 15_000;
/** How long a ping of ours may go unanswered before we call the socket dead. */
const PONG_TIMEOUT_MS = 5_000;

export function GraphQLProvider({ children }: { children: React.ReactNode }) {
  const [client, ssr] = useMemo(() => {
    const ssr = ssrExchange({ isClient: true });
    // Held across reconnects by the callbacks below: the watchdog needs the socket it is timing.
    let activeSocket: WebSocket | undefined;
    let pongTimer: number | undefined;

    const wsClient =
      typeof window !== "undefined"
        ? createWSClient({
            url: WS_URL,
            connectionParams: () => {
              const token = getAccessToken();
              return token ? { Authorization: `Bearer ${token}` } : {};
            },
            // Ping the gateway ourselves rather than only answering its pings. A laptop coming back
            // from sleep leaves a half-open socket: the peer is gone, no close event ever arrives,
            // and a client that only listens waits on it forever — every subscription silently
            // dead, with a page reload the only way back. Writing into the socket is what surfaces
            // the break, and the pong watchdog below is what acts on it.
            keepAlive: KEEP_ALIVE_MS,
            // Retry for as long as the tab is open. The default gives up after five attempts,
            // which a wake burns through while the network is still coming up — and then nothing
            // reconnects, again leaving a reload as the only cure.
            retryAttempts: Infinity,
            on: {
              connected: (socket) => {
                activeSocket = socket as WebSocket;
              },
              ping: (received) => {
                // Only our own pings start the clock; a server ping is answered by the library.
                if (received) return;
                pongTimer = window.setTimeout(() => {
                  if (activeSocket?.readyState === WebSocket.OPEN) {
                    // Closing is what triggers the client's own retry.
                    activeSocket.close(4408, "Request Timeout");
                  }
                }, PONG_TIMEOUT_MS);
              },
              pong: (received) => {
                if (received && pongTimer !== undefined) {
                  window.clearTimeout(pongTimer);
                  pongTimer = undefined;
                }
              },
            },
          })
        : null;

    const exchanges = [
      cacheExchange,
      ssr,
      fetchExchange,
      ...(wsClient
        ? [
            subscriptionExchange({
              forwardSubscription(request) {
                const input = { ...request, query: request.query || "" };
                return {
                  subscribe(sink) {
                    const unsubscribe = wsClient.subscribe(input, sink);
                    return { unsubscribe };
                  },
                };
              },
            }),
          ]
        : []),
    ];

    const client = createClient({
      url: GRAPHQL_URL,
      preferGetMethod: false,
      exchanges,
      fetchOptions: () => {
        const token = getAccessToken();
        return token ? { headers: { Authorization: `Bearer ${token}` } } : {};
      },
    });
    return [client, ssr] as const;
  }, []);

  return (
    <UrqlProvider client={client} ssr={ssr}>
      {children}
    </UrqlProvider>
  );
}
