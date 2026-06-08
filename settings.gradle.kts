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
            version("spring-security", "7.0.5")
            version("spring-cloud", "2025.1.1")
            version("opentelemetry", "2.28.1")
            version("spring-grpc", "1.0.3")

            // matching proto version from spring-grpc
            // https://repo1.maven.org/maven2/org/springframework/grpc/spring-grpc-dependencies/1.0.3/spring-grpc-dependencies-1.0.3.pom
            version("protoc-protobuf", "4.33.4")
            version("protoc-grpc", "1.77.1")

            // Other dependencies
            version("keycloak", "26.0.9")
            version("mapstruct", "1.6.3")
            version("spi-protobuf-mapstruct", "1.58.0")
            version("shedlock", "7.7.0")
            version("testcontainers", "2.0.5")

            // Plugins
            plugin("spring-boot", "org.springframework.boot").version("4.0.6")
            plugin("spotbugs", "com.github.spotbugs").version("6.5.5")
            plugin("protobuf", "com.google.protobuf").version("0.10.0")
            plugin("buf", "build.buf").version("0.11.0")
            plugin("lombok", "io.freefair.lombok").version("9.5.0")
        }
    }
}
