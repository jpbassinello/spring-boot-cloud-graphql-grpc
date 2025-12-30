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
            version("spring-security", "7.0.2")
            version("spring-cloud", "2025.1.0")
            version("opentelemetry", "2.23.0")
            version("spring-grpc", "1.0.0")

            // matching proto version from spring-grpc
            version("protoc-protobuf", "4.33.1")
            version("protoc-grpc", "1.77.1")

            // Other dependencies
            version("keycloak", "26.0.7")
            version("mapstruct", "1.6.3")
            version("spi-protobuf-mapstruct", "1.49.0")
            version("shedlock", "7.5.0")
            version("testcontainers", "2.0.3")

            // Plugins
            plugin("spring-boot", "org.springframework.boot").version("4.0.1")
            plugin("spotbugs", "com.github.spotbugs").version("6.4.8")
            plugin("protobuf", "com.google.protobuf").version("0.9.6")
            plugin("buf", "com.parmet.buf").version("0.8.5")
            plugin("lombok", "io.freefair.lombok").version("9.1.0")
        }
    }
}
