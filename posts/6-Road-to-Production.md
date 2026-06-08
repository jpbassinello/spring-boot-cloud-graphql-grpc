# Road to Production: Running a Robust Microservices Backend for a Few Dollars a Month

This is the sixth post in the SBCGG series. If you haven't read the previous ones, check them
out: [Introducing SBCGG](1-About-SBCGG.md), [gRPC in SBCGG](2-gRPC-in-SBCGG.md),
[Testcontainers with Gradle Test Fixtures](3-Testcontainers-with-Gradle-Test-Fixtures.md),
[Claude Skills for Dependency Upgrades](4-Claude-Skills-for-Dependency-Upgrades.md),
and [Observability in SBCGG](5-Observability-in-SBCGG.md).

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

---

## From Reference Project to Real Product

When I started SBCGG, the goal was to consolidate my learnings and build a foundation I could reuse for my own side
projects. That second part finally happened: I forked the repo and used it as the backbone for a real product,
**[RoleSense](https://role-sense.com/)**.

This post is about what it actually took to get that foundation into production. The architecture is the same one
described in the previous posts: a GraphQL gateway, gRPC services, PostgreSQL, Redis, Consul for service discovery,
Keycloak for identity, and the LGTM stack for observability. That's a lot of moving parts. The kind of stack that, on a
typical cloud provider, quickly turns into a four-figure monthly bill once you spin up managed databases, managed
caches, load balancers, and a Kubernetes cluster.

I wanted to prove the opposite. That even a backend this robust can run for **less than the price of a couple of
coffees a month**, as long as you make a few deliberate decisions.

Here's the punchline up front:

| Component                      | Provider           | Monthly Cost |
|--------------------------------|--------------------|--------------|
| Compute (8GB VM)               | Hetzner            | ~$15         |
| Domain, DNS, TLS, WAF          | Cloudflare         | $0           |
| Object storage (S3-compatible) | Cloudflare R2      | $0           |
| Transactional email            | Brevo              | $0           |
| Frontend hosting               | Netlify            | $0           |
| **Total**                      |                    | **< $20**    |

Let me walk through each decision.

## Compute: One 8GB VM, Docker Only

The first decision was the biggest one: **no Kubernetes**.

SBCGG ships with both Docker Compose and Kubernetes manifests. Kubernetes is the right answer when you need horizontal
scaling, rolling deployments across nodes, and self-healing across a cluster. But for a product that's just getting off
the ground, a managed Kubernetes cluster is pure overhead. You pay for the control plane, the worker nodes, the load
balancer, and you inherit a mountain of operational complexity for traffic you don't have yet.

So I went the other way: a single VM running **Docker and Docker Compose, nothing else**.

I picked [Hetzner](https://www.hetzner.com/) for the hardware. An 8GB shared-vCPU instance (the CX line) costs around
**€15/month**, and that 8GB matters. Between PostgreSQL, Redis, Keycloak, Consul, the LGTM container, the GraphQL
gateway, and the gRPC services, memory is the real constraint, not CPU. The same spec on the big-name clouds costs two
to three times as much, before you even add the managed services on top.

The deployment model is deliberately boring:

- Everything runs as a container on one host, defined in a single Docker Compose file.
- PostgreSQL and Redis run as containers too, with their data on mounted volumes. No managed database, no managed cache.
- A deploy is a `git pull` and a `docker compose up -d`. No CI cluster, no orchestrator, no Helm charts.

This is the part people resist, because we've been trained to treat "self-hosted database in a container" as a sin. And
at scale, it is. But for a product finding its first users, a containerized Postgres with a mounted volume and regular
backups is perfectly reasonable, and it costs nothing beyond the VM it already runs on. The migration path to a managed
database later is a connection string change, not a rewrite. The whole point of the SBCGG foundation is that this swap
is a configuration change.

The previous posts already did the hard work here. Because every service exports telemetry over standard OTLP, runs the
same way in Compose as in Kubernetes, and discovers its peers through Consul, the jump from "runs on my laptop" to "runs
on a VM" was almost entirely about networking and secrets, not about rearchitecting anything.

## Domain, TLS, and Protection: Cloudflare

The domain for RoleSense is managed entirely through [Cloudflare](https://www.cloudflare.com/). This single decision
quietly removes a whole category of work.

- **DNS**: managed in a clean dashboard, with instant propagation.
- **TLS certificates**: Cloudflare terminates HTTPS at its edge and issues and renews certificates automatically. No
  Let's Encrypt cron jobs, no certbot, no expiry alerts at 2 a.m.
- **Protection**: the VM sits behind Cloudflare's proxy. The origin IP is hidden, and you get DDoS protection, a WAF,
  and rate limiting on the free tier. Traffic to the backend only ever arrives through Cloudflare.

For a single VM exposed to the public internet, that last point is not a nice-to-have. Putting Cloudflare in front of
the origin means the box is never directly addressable, which dramatically shrinks the attack surface for a setup that
has no dedicated security team behind it.

## Exposing Grafana and Consul: Traefik as the Edge Router

Here's a problem the local setup never had to solve. On my laptop, Grafana lives at `localhost:3100` and the Consul
dashboard at `localhost:8500`. In production, I still want to reach both, but I absolutely do not want to expose them to
the open internet. These dashboards leak a lot about the system's internals.

The answer was to put a [Traefik](https://traefik.io/traefik/) container in front of everything as a reverse proxy and
edge router. Traefik became the single entry point on the VM, routing by subdomain:

- `api.role-sense.com` → the GraphQL gateway
- `grafana.role-sense.com` → the LGTM Grafana UI
- `consul.role-sense.com` → the Consul dashboard

The two internal dashboards sit behind **HTTP basic auth** (the browser username/password prompt), configured as a
Traefik middleware. It's not fancy, but for an operator-only dashboard behind Cloudflare's edge, a basic-auth gate is a
completely appropriate second lock. Combined with Cloudflare in front, you'd need to get past the edge proxy and then
past the credential prompt to see anything.

Traefik also has a clean Docker integration: it reads labels off the containers themselves, so each service declares its
own routing rules right in the Compose file. Adding a new subdomain is a few labels, not a separate config file.

```yaml
# Sketch of how a service declares its own routing
labels:
  - "traefik.enable=true"
  - "traefik.http.routers.grafana.rule=Host(`grafana.role-sense.com`)"
  - "traefik.http.routers.grafana.middlewares=auth"
  - "traefik.http.middlewares.auth.basicauth.users=admin:$$apr1$$..."
```

This was the one genuinely new piece of infrastructure the production deploy required that the SBCGG foundation didn't
already have. Everything else was reused.

## Object Storage: Cloudflare R2

The product needs to store files. The reflex here is "spin up an S3 bucket," and then you're back on a metered cloud
bill with egress fees that are notoriously hard to predict.

[Cloudflare R2](https://www.cloudflare.com/developer-platform/products/r2/) is S3-compatible object storage with a free
tier that's generous enough to run a young product at **$0**, and critically, it has **no egress fees**. Because it
speaks the S3 API, any existing S3 client library just works against it. It was a credentials-and-endpoint change, not a
new integration.

Keeping storage inside Cloudflare also means the domain, DNS, TLS, protection, and files all live with one provider and
one dashboard. Fewer accounts, fewer bills, fewer places for something to silently break.

## Transactional Email: Brevo

The original SBCGG use case, from the very first post, is a user signing up and receiving a message with an
authorization code. In production, that means actually sending email reliably, which is its own rabbit hole: SMTP
servers, deliverability, SPF/DKIM, and the constant risk of landing in spam.

I went with [Brevo](https://www.brevo.com/) and its simple HTTP API. The free tier allows **300 emails per day**, which
is plenty for sign-up codes, password resets, and notifications while the user base is small. Instead of running and
babysitting a mail server, the messages service makes an API call and Brevo handles delivery and reputation.

The clean part is that this slots directly into the hexagonal architecture from the earlier posts. Sending email is an
**output port**. The domain doesn't know or care that Brevo exists; it just calls a port. Swapping Brevo for SES,
Postmark, or anything else later is a new adapter, not a change to any business logic. The architecture rules in the
project's `CLAUDE.md` aren't just for show; this is exactly the seam they're meant to protect.

## Frontend: TypeScript + React on Netlify

SBCGG is a backend project. It exposes a GraphQL API and stops there. RoleSense needed an actual UI, so I built one in
**TypeScript + React** and hosted it on [Netlify](https://www.netlify.com/).

Netlify's free tier covers a static single-page app comfortably: global CDN, automatic HTTPS, and deploy-on-push from
the Git repo. The React app talks to `api.role-sense.com`, which is the GraphQL gateway sitting behind Traefik and
Cloudflare. Because the frontend is a static bundle served from a CDN, it never touches the VM, which keeps load off the
one box that's actually doing work.

This also keeps a clean split: the expensive, stateful, complex part (the backend) lives on the VM I control, and the
cheap, stateless, cacheable part (the frontend) lives on a CDN for free.

## What This Proves

Add it all up and the entire production stack, a GraphQL gateway, multiple gRPC services, PostgreSQL, Redis, Keycloak,
Consul, full observability, object storage, transactional email, and a React frontend, runs for **under $20 a month**,
almost all of which is the single VM.

The lesson isn't "never use managed services" or "Kubernetes is bad." Both have their place, and the SBCGG foundation
ships with the Kubernetes manifests for exactly the day they're needed. The lesson is about **matching infrastructure to
the stage you're actually at**. A robust, well-architected backend does not require an expensive deployment to be
production-ready. It requires a few deliberate choices:

- Collapse the cluster into a single well-sized VM, and let Docker Compose orchestrate it.
- Push the commodity concerns (DNS, TLS, protection, storage, email, frontend hosting) onto generous free tiers.
- Lean on the architecture's seams (ports and adapters, OTLP, service discovery) so that scaling up later is a
  configuration change, not a rewrite.

The whole reason SBCGG was worth building as a reference is that it makes this path realistic. The same code that runs on
a laptop runs on a $15 VM, and the day it needs to run on a cluster, it can, without touching the domain logic. That's
the payoff of the previous five posts, made concrete.

---

## What's Next?

In the next posts, I plan to cover:

- **Hexagonal Architecture in production microservices projects**

---

I'd love to hear your feedback. If you think this could help other developers, please share it.

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

Thank you for reading.
