subprojects {
    dependencies {
        api(project(":shared:grpc-client"))

        api("org.springframework.boot:spring-boot-starter-graphql")
        api("org.springframework.boot:spring-boot-starter-security")
        api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
        api("org.keycloak:keycloak-spring-boot-starter:${rootProject.libs.versions.keycloak.get()}")

        testImplementation("org.springframework.graphql:spring-graphql-test")
        testImplementation("org.springframework.security:spring-security-test")
    }
}