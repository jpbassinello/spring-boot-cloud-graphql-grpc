import com.github.spotbugs.snom.SpotBugsTask

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.spotbugs) apply false
    alias(libs.plugins.lombok) apply false
    // Core plugins
    id("java-library")
    id("checkstyle")
    id("jacoco")
}

allprojects {
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.spotbugs")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        // importing BOMs
        // https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import
        implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
        implementation(platform("org.springframework.security:spring-security-bom:${rootProject.libs.versions.spring.security.get()}"))
        implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${rootProject.libs.versions.spring.cloud.get()}"))
        implementation(platform("org.springframework.grpc:spring-grpc-dependencies:${rootProject.libs.versions.spring.grpc.get()}"))
        // Boot's grpc-bom pins an older grpc than spring-grpc-core's direct dependency, which
        // skews grpc-netty behind grpc-core at runtime (AbstractMethodError starting the gRPC
        // server). Re-import grpc-bom at the catalog version so the whole io.grpc family moves
        // together with the protoc-gen-grpc-java that generates our stubs.
        implementation(platform("io.grpc:grpc-bom:${rootProject.libs.versions.protoc.grpc.get()}"))
        implementation(platform("org.testcontainers:testcontainers-bom:${rootProject.libs.versions.testcontainers.get()}"))
        implementation("com.github.spotbugs:spotbugs-annotations:4.10.4")

        annotationProcessor("org.mapstruct:mapstruct-processor:${rootProject.libs.versions.mapstruct.get()}")
        annotationProcessor("no.entur.mapstruct.spi:protobuf-spi-impl:${rootProject.libs.versions.spi.protobuf.mapstruct.get()}")

        // JUnit platform launcher for all test executions
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
    }

    // The BOMs above are added to `implementation`, which testFixtures configurations do not
    // extend, so a module with test fixtures would otherwise have to re-import every platform in
    // its own build file just to write an unversioned testFixtures dependency.
    plugins.withId("java-test-fixtures") {
        dependencies {
            "testFixturesImplementation"(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
            "testFixturesImplementation"(platform("org.testcontainers:testcontainers-bom:${rootProject.libs.versions.testcontainers.get()}"))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        jvmArgs("-XX:+EnableDynamicAgentLoading", "--sun-misc-unsafe-memory-access=allow")
    }

    tasks.withType<JavaExec>().configureEach {
        // Silence JEP 498 warning from protobuf-java's sun.misc.Unsafe usage
        jvmArgs("--sun-misc-unsafe-memory-access=allow")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    checkstyle {
        toolVersion = "14.1.0"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    }

    tasks.withType<SpotBugsTask> {
        excludeFilter.set(rootProject.file("config/spotbugs/spotbugs_ignore.xml"))
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
