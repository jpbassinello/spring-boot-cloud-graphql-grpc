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
        implementation(platform("org.testcontainers:testcontainers-bom:${rootProject.libs.versions.testcontainers.get()}"))
        implementation("com.github.spotbugs:spotbugs-annotations:4.9.8")

        annotationProcessor("org.mapstruct:mapstruct-processor:${rootProject.libs.versions.mapstruct.get()}")
        annotationProcessor("no.entur.mapstruct.spi:protobuf-spi-impl:${rootProject.libs.versions.spi.protobuf.mapstruct.get()}")

        // JUnit platform launcher for all test executions
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    checkstyle {
        toolVersion = "13.2.0"
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
