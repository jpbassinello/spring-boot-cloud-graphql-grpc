plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":shared:validation"))

    api("com.zaxxer:HikariCP")
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    api("org.springframework.boot:spring-boot-starter-flyway")
    api("org.flywaydb:flyway-database-postgresql")
    api("org.postgresql:postgresql")
    api("com.github.f4b6a3:uuid-creator:6.1.1")

    testFixturesApi(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    testFixturesApi(platform("org.testcontainers:testcontainers-bom:${rootProject.libs.versions.testcontainers.get()}"))
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")
    testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
    testFixturesApi("org.testcontainers:testcontainers-postgresql")
}