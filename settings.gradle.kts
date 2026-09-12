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
            version("spring-security", "7.0.5")
            version("spring-cloud", "2025.1.1")
            // OTel *instrumentation* version (logback-appender). MUST track the OTel core SDK
            // that the Spring Boot BOM pins, since API and SDK must match. Instrumentation 2.N
            // maps to core 1.(N+34). Bumping past the matching pair drags opentelemetry-api ahead
            // of the constrained sdk-logs and crashes at startup with NoClassDefFoundError.
            version("opentelemetry", "2.28.1")
            version("spring-grpc", "1.0.3")

            // protoc MUST match the *resolved* runtime versions or generated code fails its
            // static-init version check: protobuf-java comes from the Boot BOM, grpc from
            // spring-grpc-core's direct pin (which wins the conflict over Boot's grpc-bom, hence
            // the grpc-bom re-import in build.gradle.kts). Verify with dependencyInsight on
            // protobuf-java / grpc-core after bumping Boot or spring-grpc.
            // https://repo1.maven.org/maven2/org/springframework/grpc/spring-grpc-dependencies/1.0.3/spring-grpc-dependencies-1.0.3.pom
            version("protoc-protobuf", "4.33.4")
            version("protoc-grpc", "1.77.1")

            // Other dependencies
            // keycloak-admin-client is published separately and capped at 26.0.x on Maven Central
            // (the Keycloak *server* image in docker compose tracks a newer line independently)
            version("keycloak", "26.0.9")
            version("mapstruct", "1.6.3")
            version("spi-protobuf-mapstruct", "1.58.0")
            version("shedlock", "7.7.0")
            version("testcontainers", "2.0.5")

            // Plugins
            plugin("spring-boot", "org.springframework.boot").version("4.0.6")
            // held at 6.5.5: 6.5.6+ ships a stricter NP detector that flags latent issues in
            // MapStruct-generated mappers not covered by spotbugs_ignore.xml
            plugin("spotbugs", "com.github.spotbugs").version("6.5.5")
            plugin("protobuf", "com.google.protobuf").version("0.10.0")
            plugin("buf", "build.buf").version("0.11.0")
            plugin("lombok", "io.freefair.lombok").version("9.5.0")
        }
    }
}
