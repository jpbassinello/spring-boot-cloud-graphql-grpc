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
include(":shared:exception")
include(":shared:grpc-client")
include(":shared:grpc-server")
include(":shared:mapping")
include(":shared:proto-mapping")
include(":shared:spring-app")
include(":shared:spring-cache")
include(":shared:spring-jpa")
include(":shared:spring-shedlock")
include(":shared:util")
include(":shared:validation")

include(":tests")
include(":tests:e2e")

// Define dependency versions
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("spring-security", "6.5.7")
            version("spring-cloud", "2025.0.1")
            version("opentelemetry", "2.23.0")

            // gRPC
            version("grpc", "1.77.0")
            version("protobuf", "4.33.2")
            version("devh-grpc-spring-boot-starter", "3.1.0.RELEASE")

            // Other dependencies
            version("keycloak", "25.0.3")
            version("mapstruct", "1.6.3")
            version("spi-protobuf-mapstruct", "1.49.0")
            version("shedlock", "6.10.0")

            // Plugins
            plugin("spring-boot", "org.springframework.boot").version("3.5.9")
            plugin("spotbugs", "com.github.spotbugs").version("6.4.8")
            plugin("protobuf", "com.google.protobuf").version("0.9.6")
            plugin("buf", "com.parmet.buf").version("0.8.5")
            plugin("lombok", "io.freefair.lombok").version("9.1.0")
        }
    }
}
