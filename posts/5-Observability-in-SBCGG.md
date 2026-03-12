# Observability in SBCGG: Logs, Traces, and Metrics with Spring Boot 4 and Grafana LGTM

This is the fifth post in the SBCGG series. If you haven't read the previous ones, check them
out: [Introducing SBCGG](1-About-SBCGG.md), [gRPC in SBCGG](2-gRPC-in-SBCGG.md),
[Testcontainers with Gradle Test Fixtures](3-Testcontainers-with-Gradle-Test-Fixtures.md),
and [Claude Skills for Dependency Upgrades](4-Claude-Skills-for-Dependency-Upgrades.md).

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

---

## Why Observability Matters in Microservices

When a request enters SBCGG, it flows through the GraphQL gateway, into a gRPC service, possibly into another gRPC
service, hits a database, checks a cache, and returns. That's four or five network hops for a single user query.

When something goes wrong, the question is never "is there a bug?" but rather "where is the bug?". Did the gateway
timeout? Did the users service fail to connect to PostgreSQL? Did the messages service return a gRPC error? Was it slow,
or was it broken?

Without observability, you're reading logs from three different containers, trying to correlate timestamps manually, and
guessing which request caused which error. With observability, you get a single trace that shows the entire request flow,
correlated logs from every service involved, and metrics that tell you if this is a one-off or a pattern.

Observability is built on three pillars:

- **Logs**: What happened. Structured events from each service.
- **Traces**: How it happened. The full path of a request across services, with timing for each hop.
- **Metrics**: How often and how fast. Counters, gauges, and histograms that reveal patterns over time.

The key is **correlation**. A trace ID that appears in every log line, across every service, for a single request. That
turns three isolated log streams into one coherent story.

## How Spring Boot 4 Improved Observability

Spring Boot has supported observability for a while, but Spring Boot 4 made it significantly better. The improvements
are not just incremental; they changed how you set up and configure the entire observability stack.

### First-Class OpenTelemetry Support

Spring Boot 4 introduced `spring-boot-starter-opentelemetry`, a single starter that wires up the OpenTelemetry SDK,
auto-configures exporters, and integrates with Micrometer. Before this, you had to manually configure the OpenTelemetry
SDK, set up the exporter beans, and bridge Micrometer to OpenTelemetry yourself. It was doable, but it was boilerplate
that every project had to repeat.

In SBCGG, the entire observability dependency setup lives in a single shared module (`shared/spring-app/build.gradle.kts`):

```kotlin
dependencies {
    api("org.springframework.boot:spring-boot-starter-opentelemetry")
    api("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:${libs.versions.opentelemetry.get()}-alpha")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("org.springframework.boot:spring-boot-starter-actuator")
}
```

Every service in the project inherits from this shared module. No service needs to declare its own observability
dependencies. Four lines give you tracing, metrics, and log export via OTLP.

### Declarative Configuration via `application.yaml`

Spring Boot 4 moved OpenTelemetry configuration into the standard `management.*` namespace. You configure tracing,
metrics, and log export the same way you configure any other Spring Boot feature: through properties.

Here's the full observability configuration for SBCGG's Docker environment (`application-dev-docker.yaml`):

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    metrics:
      export:
        enabled: true
        url: http://lgtm:4318/v1/metrics
  opentelemetry:
    tracing:
      export:
        otlp:
          enabled: true
          endpoint: http://lgtm:4318/v1/traces
    logging:
      export:
        otlp:
          enabled: true
          endpoint: http://lgtm:4318/v1/logs
```

That's it. No Java configuration needed for the exporter setup. Spring Boot auto-configures the OTLP exporters based on
these properties. The `sampling.probability: 1.0` means every request gets traced, which is what you want in development.
In production, you'd lower this to reduce overhead.

### OpenTelemetry Semantic Conventions for Metrics

Spring Boot 4 added native support for OpenTelemetry semantic conventions in Micrometer. This means JVM metrics (CPU,
memory, threads, class loading) use standardized metric names that any OpenTelemetry-compatible backend understands out
of the box.

In SBCGG, we take advantage of this with a configuration class that registers JVM metrics using OpenTelemetry naming
conventions:

```java
@Configuration(proxyBeanMethods = false)
class OpenTelemetryConfig {

  OpenTelemetryConfig(OpenTelemetry openTelemetry) {
    OpenTelemetryAppender.install(openTelemetry);
  }

  @Bean
  OpenTelemetryServerRequestObservationConvention
      openTelemetryServerRequestObservationConvention() {
    return new OpenTelemetryServerRequestObservationConvention();
  }

  @Bean
  ProcessorMetrics processorMetrics() {
    return new ProcessorMetrics(List.of(),
        new OpenTelemetryJvmCpuMeterConventions(Tags.empty()));
  }

  @Bean
  JvmMemoryMetrics jvmMemoryMetrics() {
    return new JvmMemoryMetrics(List.of(),
        new OpenTelemetryJvmMemoryMeterConventions(Tags.empty()));
  }

  @Bean
  JvmThreadMetrics jvmThreadMetrics() {
    return new JvmThreadMetrics(List.of(),
        new OpenTelemetryJvmThreadMeterConventions(Tags.empty()));
  }

  @Bean
  ClassLoaderMetrics classLoaderMetrics() {
    return new ClassLoaderMetrics(
        new OpenTelemetryJvmClassLoadingMeterConventions());
  }
}
```

The `OpenTelemetryServerRequestObservationConvention` bean ensures that HTTP server metrics follow the OpenTelemetry
semantic convention naming (e.g., `http.server.request.duration` instead of Micrometer's default
`http.server.requests`). This matters when you use Grafana dashboards designed for OpenTelemetry data, as they expect
these standardized metric names.

The JVM metric beans (`ProcessorMetrics`, `JvmMemoryMetrics`, `JvmThreadMetrics`, `ClassLoaderMetrics`) each receive an
OpenTelemetry convention object that remaps their metric names to the OTel standard. Instead of Micrometer's
`jvm.memory.used`, you get `jvm.memory.used` with OpenTelemetry-compliant attribute names and units.

### Log Correlation via OpenTelemetry Appender

The `OpenTelemetryAppender.install(openTelemetry)` call in the constructor is what bridges Logback to OpenTelemetry.
Every log statement automatically includes the active trace ID and span ID, which means when you search for a trace in
Grafana Tempo, you can jump directly to the correlated logs in Loki.

The logback configuration (`logback-spring.xml`) activates this only in the Docker profile:

```xml
<springProfile name="dev-docker">
    <appender name="OpenTelemetry"
              class="io.opentelemetry.instrumentation.logback
                     .appender.v1_0.OpenTelemetryAppender">
        <captureCodeAttributes>true</captureCodeAttributes>
        <captureMarkerAttribute>true</captureMarkerAttribute>
        <captureKeyValuePairAttributes>true</captureKeyValuePairAttributes>
        <captureLoggerContext>true</captureLoggerContext>
        <captureMdcAttributes>*</captureMdcAttributes>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="OpenTelemetry"/>
    </root>
</springProfile>
```

The `captureCodeAttributes: true` setting includes the source file and line number in every log entry. The
`captureMdcAttributes: *` captures all MDC attributes, which is where Spring puts the trace context. In local
development (`dev` profile), logs go to the console only, so there's no overhead.

### Profile-Based Activation

This is a subtle but important detail. SBCGG doesn't enable observability export everywhere. The `dev` profile
(local development) disables all OTLP export for fast startup and zero overhead. The `dev-docker` profile enables
everything because LGTM is running as a Docker container. Kubernetes uses environment variables from a ConfigMap to point
at the LGTM service.

This means developers aren't paying for observability overhead during local development, but the moment they run
`docker-compose up`, they get full traces, metrics, and logs without changing any code.

## LGTM: One Container for the Entire Observability Stack

SBCGG uses the [Grafana LGTM](https://grafana.com/docs/opentelemetry/docker-lgtm/) all-in-one image. LGTM stands for
**Loki** (logs), **Grafana** (visualization), **Tempo** (traces), and **Mimir** (metrics). It's a single Docker image
that bundles all four components.

### Why LGTM?

The traditional approach to observability infrastructure involves running and configuring multiple services: a metrics
backend (Prometheus), a trace backend (Jaeger or Zipkin), a log aggregation system (ELK or Fluentd), and a
visualization layer (Grafana). Each one needs its own deployment, configuration, and maintenance.

For a development and learning environment like SBCGG, this is overkill. The Grafana LGTM image gives you the same
capabilities in a single container:

```yaml
# infrastructure/docker/compose/dev/docker-compose.yml
lgtm:
  image: grafana/otel-lgtm:0.20.0
  container_name: sbcgg-lgtm
  ports:
    - "4317:4317"   # OTLP gRPC receiver
    - "4318:4318"   # OTLP HTTP receiver
    - "3100:3000"   # Grafana UI
  deploy:
    resources:
      limits:
        memory: 1G
        cpus: "1"
      reservations:
        memory: 512M
```

Three ports. That's all the configuration you need. Port `4317` and `4318` receive telemetry data via OTLP (gRPC and
HTTP respectively). Port `3100` serves the Grafana UI where you visualize everything.

SBCGG uses the HTTP OTLP endpoint (`4318`) for all three signals. The services send traces to `/v1/traces`, metrics to
`/v1/metrics`, and logs to `/v1/logs`, all on the same host and port. There is no collector to configure, no separate
storage to provision, and no retention policies to manage.

### What You Get Out of the Box

Open `http://localhost:3100` and you have:

- **Loki**: Query logs from all services. Filter by service name, trace ID, log level, or any MDC attribute. Since logs
  include trace IDs, you can jump from a log entry directly to the corresponding trace.
- **Tempo**: View distributed traces. See the full request path from the GraphQL gateway through gRPC services, with
  timing for each span. Identify which service or database call is the bottleneck.
- **Mimir**: Query metrics using PromQL. View JVM memory usage, HTTP request latency, gRPC call durations, cache hit
  rates, and any custom metric. Build dashboards or use pre-built ones.
- **Grafana**: The unified UI that ties it all together. Create dashboards that combine metrics, traces, and logs.
  Set up alerts. Correlate a spike in error rate with specific traces and their logs.

### Kubernetes Deployment

For Kubernetes, LGTM uses the same image with proper resource limits and health checks:

```yaml
# infrastructure/kubernetes/infrastructure/lgtm/deployment.yaml
containers:
  - name: lgtm
    image: grafana/otel-lgtm:0.20.0
    ports:
      - containerPort: 3000    # Grafana
      - containerPort: 4317    # OTLP gRPC
        name: otlp-grpc
      - containerPort: 4318    # OTLP HTTP
        name: otlp-http
    resources:
      requests:
        cpu: 250m
        memory: 512Mi
      limits:
        cpu: 1000m
        memory: 2Gi
```

Services in Kubernetes reach LGTM via the internal DNS name `lgtm.sbcgg.svc.cluster.local`, configured through a
ConfigMap:

```yaml
MANAGEMENT_OTLP_METRICS_EXPORT_URL: "http://lgtm.sbcgg.svc.cluster.local:4318/v1/metrics"
MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT: "http://lgtm.sbcgg.svc.cluster.local:4318/v1/traces"
MANAGEMENT_OPENTELEMETRY_LOGGING_EXPORT_OTLP_ENDPOINT: "http://lgtm.sbcgg.svc.cluster.local:4318/v1/logs"
```

## The Flow: From Code to Dashboard

Here's what happens when a request enters SBCGG with observability enabled:

1. A GraphQL query hits the gateway. Spring creates a trace with a unique trace ID and starts a root span.
2. The gateway calls the users gRPC service. The trace context (trace ID + parent span ID) propagates automatically via
   gRPC metadata headers. A new child span is created in the users service.
3. The users service queries PostgreSQL. Another child span captures the query duration.
4. Every `log.info()` or `log.error()` call in any service automatically includes the trace ID in the log entry, via the
   OpenTelemetry Logback appender.
5. Micrometer records metrics: HTTP request duration, gRPC call duration, JVM stats.
6. All three signals (logs, traces, metrics) are exported to LGTM via OTLP HTTP.
7. In Grafana, you search for a slow request in Tempo, click on a span, jump to the correlated logs in Loki, and check
   if the latency matches a pattern in a Mimir dashboard.

The trace context propagation across gRPC calls is handled automatically by Spring gRPC's OpenTelemetry integration. You
don't write any code for this. The Micrometer tracing bridge (`micrometer-tracing-bridge-otel`) ensures that Spring's
observation API and OpenTelemetry's span API stay in sync.

## Alternatives to LGTM

LGTM is great for development and small-scale deployments, but it's not the only option. Here's how the alternatives
compare:

### Prometheus + Jaeger + ELK + Grafana

The classic stack. **Prometheus** scrapes metrics endpoints, **Jaeger** collects traces, **ELK** (Elasticsearch,
Logstash, Kibana) handles logs, and **Grafana** visualizes metrics and traces.

**Pros**: Mature ecosystem, widely adopted, extensive documentation, each component is battle-tested at massive scale.

**Cons**: Four separate systems to deploy, configure, and maintain. Different query languages (PromQL, Jaeger query,
Lucene/KQL). No native correlation between signals. Significant operational overhead.

**When to use**: Large organizations with dedicated platform teams that need fine-grained control over each component.

### Datadog / New Relic / Dynatrace

Fully managed SaaS platforms that handle all three pillars. You install an agent, and everything works.

**Pros**: Zero infrastructure to manage, polished UIs, AI-powered anomaly detection, out-of-the-box dashboards,
excellent onboarding experience.

**Cons**: Expensive at scale (pricing per host, per GB ingested, or per span). Vendor lock-in. Data leaves your
infrastructure. Can be cost-prohibitive for side projects or learning environments.

**When to use**: Teams that value time-to-value over cost, and organizations where the operational cost of
self-hosting exceeds the SaaS subscription.

### Grafana Cloud

Grafana's managed offering that runs the same Loki, Tempo, and Mimir stack you'd use on-premises. It's the cloud version
of what SBCGG runs locally with the LGTM image.

**Pros**: Same tools and query languages as self-hosted, generous free tier (50GB logs, 50GB traces, 10k metrics
series), no infrastructure to manage, seamless upgrade path from local LGTM.

**Cons**: Data leaves your infrastructure (unless using the dedicated tier). Can get expensive at high volumes.

**When to use**: Teams that like the Grafana stack but don't want to manage it. The migration path from SBCGG's local
LGTM setup to Grafana Cloud is straightforward: just change the OTLP endpoint URLs.

### OpenTelemetry Collector + Backend of Choice

Instead of sending telemetry directly to a backend, you run an **OpenTelemetry Collector** as an intermediary. The
collector receives data from your services, processes it (filtering, sampling, enriching), and forwards it to one or
more backends.

SBCGG includes a collector configuration (`infrastructure/docker/compose/configs/opentelemetry/collector-config.yaml`)
that demonstrates this pattern for scraping Keycloak metrics:

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
  prometheus:
    config:
      scrape_configs:
        - job_name: keycloak
          metrics_path: /metrics
          scrape_interval: 10s
          static_configs:
            - targets: ["keycloak:9000"]
```

**Pros**: Decouples services from backends. Switch backends without changing application code. Add processing
(batching, sampling, attribute enrichment) at the infrastructure level. Fan out to multiple backends simultaneously.

**Cons**: Another component to deploy and monitor. Adds a network hop and potential point of failure.

**When to use**: Production deployments where you need flexibility to change backends, apply sampling at the
infrastructure level, or send telemetry to multiple destinations.

### LGTM for SBCGG

For SBCGG, the Grafana LGTM all-in-one image hits the sweet spot. It's a single container that provides all three
observability backends plus Grafana, with zero configuration beyond exposing three ports. It uses the same tools and
query languages as Grafana Cloud, so the skills you develop locally transfer directly to production. And since SBCGG
uses standard OTLP for all signals, switching to any alternative backend is a configuration change, not a code change.

---

## What's Next?

In the next posts, I plan to cover:

- **Hexagonal Architecture in production microservices projects**

---

I'd love to hear your feedback. If you think this could help other developers, please share it.

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

Thank you for reading.