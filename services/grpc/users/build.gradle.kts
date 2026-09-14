dependencies {
    api(project(":grpc-interfaces:users"))
    api(project(":grpc-interfaces:messages"))
    api(project(":shared:grpc-client"))
    implementation("org.keycloak:keycloak-admin-client:${rootProject.libs.versions.keycloak.get()}")
    implementation("org.apache.commons:commons-lang3")

    // grpc-java split the in-process transport out of grpc-core into its own artifact, and
    // neither spring-boot-grpc-test nor the grpc-client starter (which brings grpc-netty) pulls
    // it in. Without it @AutoConfigureTestGrpcTransport backs off on a missing
    // InProcessServerBuilder and the test context has no GrpcChannelFactory at all.
    testImplementation("io.grpc:grpc-inprocess")
    testImplementation("org.springframework.boot:spring-boot-grpc-test")
}