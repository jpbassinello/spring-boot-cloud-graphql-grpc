dependencies {
    api(project(":shared:util"))
    api(project(":shared:exception"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.cloud:spring-cloud-starter-consul-config")
    api("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    api("org.springframework.boot:spring-boot-starter-opentelemetry")
    api("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:${libs.versions.opentelemetry.get()}-alpha")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("com.github.ben-manes.caffeine:caffeine")

}