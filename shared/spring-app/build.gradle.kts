dependencies {
    api(project(":shared:util"))
    api(project(":shared:exception"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.cloud:spring-cloud-starter-consul-config")
    api("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    api("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("com.github.ben-manes.caffeine:caffeine")

}