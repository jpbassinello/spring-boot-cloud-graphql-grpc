dependencies {
    api(project(":grpc-interfaces:users"))
    api(project(":grpc-interfaces:messages"))
    api(project(":shared:proto-mapping"))

    implementation("com.graphql-java:graphql-java-extended-scalars:24.0")
}