subprojects {
    dependencies {
        api(project(":shared:grpc-server"))
        api(project(":shared:spring-jpa"))
        api(project(":shared:proto-mapping"))

        testImplementation("org.springframework.boot:spring-boot-testcontainers")
        testImplementation("org.springframework.boot:spring-boot-data-jpa-test")
        testImplementation("org.springframework.boot:spring-boot-jdbc-test")
        testImplementation("org.springframework.boot:spring-boot-validation")
        testImplementation("org.springframework.grpc:spring-grpc-test")
        testImplementation("io.grpc:grpc-inprocess")
        testImplementation("org.testcontainers:junit-jupiter")
        testImplementation("org.testcontainers:postgresql")
    }
}