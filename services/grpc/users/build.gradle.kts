dependencies {
    api(project(":grpc-interfaces:users"))
    api(project(":grpc-interfaces:messages"))
    api(project(":shared:grpc-client"))
    implementation("org.keycloak:keycloak-admin-client:${rootProject.libs.versions.keycloak.get()}")
    implementation("org.apache.commons:commons-lang3")

    testImplementation("org.springframework.grpc:spring-grpc-test")
}