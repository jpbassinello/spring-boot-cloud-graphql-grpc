plugins {
    id("java-test-fixtures")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-validation")

    api(platform("software.amazon.awssdk:bom:2.54.17"))
    api("software.amazon.awssdk:s3")

    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")
    testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
    testFixturesApi("org.testcontainers:testcontainers-minio")
}
