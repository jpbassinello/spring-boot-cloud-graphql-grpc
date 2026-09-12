"use client";

import { useQuery } from "urql";
import { graphql } from "@/graphql/generated";
import { useRequireAuth } from "@/lib/use-require-auth";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

// The gateway's `logged` query resolves the caller from the bearer token the urql client
// attaches, so this is also the smoke test that Keycloak -> gateway auth is wired correctly.
const LoggedUserQuery = graphql(`
  query LoggedUser {
    logged {
      id
      firstName
      lastName
      email
      emailVerified
      roles
    }
  }
`);

export default function DashboardPage() {
  useRequireAuth();
  const [{ data, fetching, error }] = useQuery({ query: LoggedUserQuery });

  if (fetching) return <main className="p-10 text-muted-foreground">Loading…</main>;
  if (error) {
    return (
      <main className="p-10 text-destructive">
        {error.message.replace(/\[GraphQL\]\s*/g, "")}
      </main>
    );
  }

  const user = data?.logged;

  return (
    <main className="mx-auto max-w-2xl px-6 py-16">
      <Card>
        <CardHeader>
          <CardTitle>
            {user ? `${user.firstName} ${user.lastName}` : "Not signed in"}
          </CardTitle>
          <CardDescription>{user?.email}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-2">
          {user?.roles.map((role) => (
            <Badge key={role} variant="secondary">
              {role}
            </Badge>
          ))}
          {user && !user.emailVerified && <Badge variant="destructive">email unverified</Badge>}
        </CardContent>
      </Card>
    </main>
  );
}
