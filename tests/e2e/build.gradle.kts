dependencies {
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("commons-io:commons-io:2.21.0")

    tasks.withType<Test> {
        enabled = project.hasProperty("run:tests:e2e")
    }
}