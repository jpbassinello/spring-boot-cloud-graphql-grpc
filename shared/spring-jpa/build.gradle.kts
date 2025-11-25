dependencies {
    api(project(":shared:validation"))

    api("com.zaxxer:HikariCP")
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    api("org.postgresql:postgresql")
    api("com.github.f4b6a3:uuid-creator:6.1.1")
}