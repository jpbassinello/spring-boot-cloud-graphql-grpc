rootProject.name = "sbcgg"

// Include all modules
include(":grpc-interfaces")
include(":grpc-interfaces:messages")
include(":grpc-interfaces:users")

include(":services")
include(":services:graphql")
include(":services:graphql:gateway")
include(":services:grpc")
include(":services:grpc:messages")
include(":services:grpc:users")

include(":shared")
include(":shared:ai-client")
include(":shared:exception")
include(":shared:grpc-client")
include(":shared:grpc-server")
include(":shared:mapping")
include(":shared:proto-mapping")
include(":shared:spring-app")
include(":shared:spring-cache")
include(":shared:spring-jpa")
include(":shared:spring-ratelimit")
include(":shared:spring-shedlock")
include(":shared:spring-storage")
include(":shared:util")
include(":shared:validation")

include(":tests")
include(":tests:e2e")

// Define dependency versions
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("spring-security", "7.1.1")
            // No Spring Cloud train targets Boot 4.1.x yet: 2025.1.3 is built against Boot
            // 4.0.8. This pairing is therefore unsupported on paper, and is used deliberately —
            // both forks of this project run Boot 4.1.x against a 4.0.x-built Cloud train in
            // production. Re-check on the next Cloud release and drop this note once a train
            // declares Boot 4.1.
            version("spring-cloud", "2025.1.3")
            // OTel *instrumentation* version (logback-appender). MUST track the OTel core SDK
            // that the Spring Boot BOM pins, since API and SDK must match. Instrumentation 2.N
            // maps to core 1.(N+34), so 2.28.1 -> core 1.62.0, which is what Boot 4.1.1 pins.
            // Bumping past the matching pair drags opentelemetry-api ahead of the constrained
            // sdk-logs and crashes at startup with NoClassDefFoundError.
            version("opentelemetry", "2.28.1")
            // Also the version Boot 4.1.1 manages, so the two cannot drift apart.
            version("spring-grpc", "1.1.1")

            // protoc MUST match the *resolved* runtime versions or generated code fails its
            // static-init version check: protobuf-java comes from the Boot BOM, grpc from
            // spring-grpc-core's direct pin (which wins the conflict over Boot's grpc-bom, hence
            // the grpc-bom re-import in build.gradle.kts). Verify with dependencyInsight on
            // protobuf-java / grpc-core after bumping Boot or spring-grpc.
            // https://repo1.maven.org/maven2/org/springframework/grpc/spring-grpc-dependencies/1.1.1/spring-grpc-dependencies-1.1.1.pom
            version("protoc-protobuf", "4.35.1")
            version("protoc-grpc", "1.83.1")

            // Other dependencies
            // keycloak-admin-client is published separately and capped at 26.0.x on Maven Central
            // (the Keycloak *server* image in docker compose tracks a newer line independently)
            version("keycloak", "26.0.12")
            version("mapstruct", "1.6.3")
            version("spi-protobuf-mapstruct", "1.68.0")
            version("shedlock", "7.10.1")
            version("testcontainers", "2.0.5")

            // Plugins
            plugin("spring-boot", "org.springframework.boot").version("4.1.1")
            // held at 6.5.5: 6.5.6+ ships a stricter NP detector that flags latent issues in
            // MapStruct-generated mappers not covered by spotbugs_ignore.xml
            plugin("spotbugs", "com.github.spotbugs").version("6.5.5")
            plugin("protobuf", "com.google.protobuf").version("0.10.0")
            plugin("buf", "build.buf").version("0.11.1")
            plugin("lombok", "io.freefair.lombok").version("9.5.0")
        }
    }
}
