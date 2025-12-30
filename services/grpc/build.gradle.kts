subprojects {
    dependencies {
        api(project(":shared:grpc-server"))
        api(project(":shared:spring-jpa"))
        api(project(":shared:proto-mapping"))

        testImplementation("org.springframework.boot:spring-boot-testcontainers")
        testImplementation("org.springframework.boot:spring-boot-data-jpa-test")
        testImplementation("org.testcontainers:testcontainers-junit-jupiter")
        testImplementation("org.testcontainers:testcontainers-postgresql")
    }
}