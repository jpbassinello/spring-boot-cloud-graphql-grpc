import Link from "next/link";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

const modules = [
  {
    title: "GraphQL gateway",
    description:
      "The only service exposed to clients. Fans out to the gRPC services and maps their errors onto GraphQL error types.",
  },
  {
    title: "gRPC services",
    description:
      "users and messages, each hexagonal: domain at the core, ports in between, adapters at the edges.",
  },
  {
    title: "Shared libraries",
    description:
      "Spring base config, JPA, cache, rate limiting, object storage, distributed locking and an LLM client, wired once and reused.",
  },
];

export default function Home() {
  return (
    <main className="mx-auto flex min-h-screen max-w-4xl flex-col justify-center gap-10 px-6 py-16">
      <header className="space-y-4">
        <h1 className="text-4xl font-bold tracking-tight">Spring Boot Cloud GraphQL gRPC</h1>
        <p className="max-w-2xl text-muted-foreground">
          A microservices reference stack — Spring Boot, Consul, Keycloak, gRPC between services
          and a GraphQL gateway at the edge. This page is the starting point of the frontend
          scaffold; replace it with your own product.
        </p>
        <div className="flex gap-3">
          <Button asChild>
            <Link href="/dashboard">Open the dashboard</Link>
          </Button>
          <Button asChild variant="outline">
            <a href="http://localhost:8080/graphiql">GraphiQL</a>
          </Button>
        </div>
      </header>

      <section className="grid gap-4 sm:grid-cols-3">
        {modules.map((module) => (
          <Card key={module.title}>
            <CardHeader>
              <CardTitle className="text-base">{module.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <CardDescription>{module.description}</CardDescription>
            </CardContent>
          </Card>
        ))}
      </section>
    </main>
  );
}
